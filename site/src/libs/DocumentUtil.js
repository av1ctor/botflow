import saveAs from 'file-saver';

export default class DocumentUtil 
{

	static download(url, name, callbacks = {})
	{
		const xhr = new XMLHttpRequest();
		xhr.open('GET', url);
		xhr.responseType = 'blob';
		
		xhr.onload = () =>
		{
			saveAs(xhr.response, name);
			callbacks.onFinish && callbacks.onFinish(name);
		};

		xhr.onerror = () => 
		{
			callbacks.onError && callbacks.onError(name, xhr.statusText);
		};

		xhr.onprogress = (event) =>
		{
			callbacks.onProgress && callbacks.onProgress(name, event.loaded / event.total);
		};
		
		xhr.send();		
	}
}