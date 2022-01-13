
export default class ObjUtil
{
	static async loadRefs(refsToLoad, currentRefs, workspace)
	{
		const res = new Map(currentRefs);
	
		const toLoad = Object.values(refsToLoad);
		for(let i = 0; i < toLoad.length; i++)
		{
			const ref = toLoad[i];
			const key = JSON.stringify(ref);
			if(!currentRefs.has(key))
			{
				const objs = await workspace.loadObjs(
					ref.type + 's', 
					0, 
					30, 
					'schema.name', 
					true, 
					ref.filters, 
					{nostore: true});
				if(!objs.errors)
				{
					res.set(key, objs.data.map(ref => ({
						...ref,
						schemaObj: JSON.parse(ref.schema)
					})));
				}
			}
		}
	
		return res;
	}
}