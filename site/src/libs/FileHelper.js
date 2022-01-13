import {Upload} from 'tus-js-client';

export default class FileHelper
{
	constructor(api)
	{
		this.api = api;

		// eslint-disable-next-line no-undef
		this.baseUrl = process.env.NODE_ENV === 'development'? 
			// eslint-disable-next-line no-undef
			process.env.REACT_APP_ROBOTIKFLOW_UPLOAD_URL.replace('{{hostname}}', window.location.hostname):
			// eslint-disable-next-line no-undef
			process.env.REACT_APP_ROBOTIKFLOW_UPLOAD_URL;

	}

	__getAsEntry(parent, entry)
	{
		return new Promise((resolve) =>
		{
			let reading = 0;
			const files = [];		

			readEntry(parent, entry);

			function readEntry(parent, entry)
			{
				if (entry.isFile) 
				{
					reading++;
					entry.file(fileObj => 
					{
						reading--;
						files.push({
							parent: parent,
							file: fileObj
						});

						if (reading === 0) 
						{
							resolve(files);
						}
					});
				} 
				else if (entry.isDirectory) 
				{
					files.push({
						parent: parent,
						folder: {
							name: entry.name
						}
					});

					readReaderContent(entry.name, entry.createReader());
				}
			}

			function readReaderContent(parent, reader) 
			{
				reading++;
	
				reader.readEntries(function (entries) 
				{
					reading--;
					for (const entry of entries) 
					{
						readEntry(parent, entry);
					}
	
					if (reading === 0) 
					{
						resolve(files);
					}
				});
			}			
		});
	}

	__getAsFilesAndDirectories(parent, entry)
	{
		return new Promise((resolve) =>
		{
			let reading = 0;
			const files = [];		

			readReaderContent(parent, entry);

			function readEntry(parent, fileObj)
			{
				if (!fileObj.getFilesAndDirectories) 
				{
					reading++;
					if(fileObj.name)
					{
						reading--;
						files.push({
							parent: parent,
							file: fileObj
						});

						if (reading === 0) 
						{
							resolve(files);
						}
					}
				} 
				else
				{
					files.push({
						parent: parent,
						folder: {
							name: fileObj.name
						}
					});

					readReaderContent(fileObj.name, fileObj);
				}
			}

			function readReaderContent(parent, entry) 
			{
				reading++;
	
				entry.getFilesAndDirectories(function (entries) 
				{
					reading--;
					for (const e of entries) 
					{
						readEntry(parent, e);
					}
	
					if (reading === 0) 
					{
						resolve(files);
					}
				});
			}			
		});
	}	
	
	async getDropped(parent, fileObjs)
	{
		if(fileObjs.getFilesAndDirectories)
		{
			return await this.__getAsFilesAndDirectories(parent, fileObjs);
		}

		if(fileObjs.length === 0)
		{
			return;
		}
		
		const files = [];

		if(typeof fileObjs[0].webkitGetAsEntry === 'function') 
		{
			// NOTA: é necessário ler toda a array fileObjs primeiro, ou ela vai ser apagada pelo browser
			//		 dentro de addToUploadAsEntry() por algum motivo
			const entries = [];
			for(let i = 0; i < fileObjs.length; i++)
			{
				if (fileObjs[i].kind === 'file') 
				{
					entries.push(fileObjs[i].webkitGetAsEntry());
				}
			}

			for(let i = 0; i < entries.length; i++)
			{
				files.push(...await this.__getAsEntry(parent, entries[i]));
			}
		}
		else
		{
			fileObjs.forEach(fileObj =>
			{
				files.push({
					parent: parent,
					file: fileObj
				});
			});
		}
		
		return files;
	}	

	upload(parent, fileObj, provider = null, onProgress = null)
	{
		const api = this.api;
		let parentId = parent? parent.id: null;

		const promise = new Promise((resolve, reject) =>
		{
			const meta = {
				name: fileObj.name,
				size: fileObj.size,
				type: fileObj.type,
				date: fileObj.lastModified
			};

			if(parentId)
				meta.parentId = parentId;
			if(parent && parent.path)
				meta.parentPath = parent.path;
			if(provider && provider.id)
				meta.providerId = provider.id;
			
			const up = new Upload(fileObj, {
				endpoint: `${this.baseUrl}docs/upload`,
				// eslint-disable-next-line no-undef
				chunkSize: process.env.REACT_APP_ROBOTIKFLOW_UPLOAD_CHUNKSIZE,
				resume: false,
				retryDelays: [0, 3000, 5000, 10000, 20000],
				headers: {
					Authorization: 'Bearer ' + this.api.getToken()
				},
				metadata: {
					...meta
				},
				onAfterResponse: (req, res) =>
				{
					const id = res.getHeader('Parent-Id');
					if(id)
					{
						parentId = id;
					}
				},
				onError: (error) =>
				{
					reject([error]);
				},
				onProgress: (bytesUploaded, bytesTotal) =>
				{
					if(onProgress) 
					{
						onProgress(bytesUploaded / bytesTotal);
					}
				},
				onSuccess: async () =>
				{
					const data = {
						name: up.file.name,
						size: up.file.size,
						type: up.file.type,
						date: up.file.lastModified,
						uploadUrl: up.url,
						parentId: parentId,
						providerId: provider? provider.id: null,
					};
	
					const res = await api.requestURL(
						'POST', `${this.baseUrl}docs`, data, {});
					if(res.errors !== null)
					{
						return reject(res.errors);
					}

					return resolve(res.data);
				}
			});
		
			up.start();
		});

		return promise;
	}	
}