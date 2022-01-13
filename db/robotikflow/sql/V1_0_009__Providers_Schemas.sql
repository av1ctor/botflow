insert into public.objects_schemas (
    pub_id,
	version,
    type,
    name,
    title,
    "desc",
    icon,
	schema,
    hidden,
    statefull,
    category, 
    fields,
    created_at,
    created_by_id)
	values (
		'6086e87bd5d2eb5b98150d1c',
		1.0,
		2,
		'internalStorageProvider',
        'Internal Storage',
		'Internal Storage Provider',
		'file',
		'{"basePath":{"index":0,"title":"Base path","type":"string","hidden":true,"desc":"Base path (can be relative)"},"baseUrl":{"index":1,"title":"Base URL","type":"string","hidden":true,"desc":"Base URL"},"root":{"index":2,"title":"Root path","type":"string","hidden":true,"desc":"Root document ID"}}',
		false,
		false,
		'storage',
		'{"version":1,"type":"provider","name":"internalStorageProvider","vendor":"internal","category":"storage","title":"Internal storage","desc":"Internal storage provider","icon":"file","hidden":true,"fields":{"basePath":{"index":0,"title":"Base path","type":"string","hidden":true,"desc":"Base path (can be relative)"},"baseUrl":{"index":1,"title":"Base URL","type":"string","hidden":true,"desc":"Base URL"},"root":{"index":2,"title":"Root path","type":"string","hidden":true,"desc":"Root document ID"}}}',
		CURRENT_TIMESTAMP,
		LASTVAL()
	);

insert into public.providers_schemas (
    id,
    vendor)
	values (
	LASTVAL(),
	'internal');


insert into public.objects_schemas (
    pub_id,
	version,
    type,
    name,
    title,
    "desc",
    icon,
	schema,
    hidden,
    statefull,
    category, 
    fields,
    created_at,
    created_by_id)
	values (
		'608878d0f4f25210d8e0bafd',
		1.0,
		2,
		'driveProvider',
        'Google Drive',
		'Google Drive Provider',
		'file',
		'{"version":1,"type":"provider","name":"driveProvider","vendor":"google","category":"storage","title":"Google Drive","desc":"Google Drive Provider","icon":"svg:drive","refs":{"credentials":{"type":"credential","filters":{"vendor":"google"}}},"fields":{"name":{"index":0,"title":"Name","type":"string","desc":"Server identification"},"credential":{"index":1,"title":"Credential","type":"string","required":true,"input":{"type":"select","source":{"name":"credentials.fields","option":{"value":"id","title":"${username}@drive"}}},"desc":"Credential to use when acessing the service"},"basePath":{"index":2,"title":"Base path","type":"string","required":true,"desc":"Base path to use (folder''s name inside Google Drive)"},"root":{"index":3,"title":"Root path","type":"string","hidden":true,"desc":"Root document ID"}},"methods":{"toString":{"script":"fields.name? fields.name: lookup(''credentials'', fields.credential).username + ''@drive''"}}}',
		false,
		true,
		'storage',
		'{"name":{"index":0,"title":"Name","type":"string","desc":"Server identification"},"credential":{"index":1,"title":"Credential","type":"string","required":true,"input":{"type":"select","source":{"name":"credentials.fields","option":{"value":"id","title":"${username}@drive"}}},"desc":"Credential to use when acessing the service"},"basePath":{"index":2,"title":"Base path","type":"string","required":true,"desc":"Base path to use (folder''s name inside Google Drive)"},"root":{"index":3,"title":"Root path","type":"string","hidden":true,"desc":"Root document ID"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.providers_schemas (
    id,
    vendor)
	values (
	LASTVAL(),
	'google');


insert into public.objects_schemas (
    pub_id,
	version,
    type,
    name,
    title,
    "desc",
    icon,
	schema,
    hidden,
    statefull,
    category, 
    fields,
    created_at,
    created_by_id)
	values (
		'608c579d78c2eb5ade8d0fae',
		1.0,
		2,
		'gmailProvider',
        'Gmail',
		'Gmail Provider',
		'envelope',
		'{"version":1,"type":"provider","name":"gmailProvider","vendor":"google","category":"email","title":"Gmail","desc":"Gmail Provider","icon":"svg:gmail","refs":{"credentials":{"type":"credential","filters":{"vendor":"google"}}},"fields":{"name":{"index":0,"title":"Name","type":"string","desc":"Server identification"},"credential":{"index":1,"title":"Credential","type":"string","required":true,"input":{"type":"select","source":{"name":"credentials.fields","option":{"value":"id","title":"${username}@gmail.com"}}},"desc":"Credential to use when acessing the service"}},"methods":{"toString":{"script":"fields.name? fields.name: lookup(''credentials'', fields.credential).username + ''@gmail.com''"}}}',
		false,
		true,
		'email',
		'{"name":{"index":0,"title":"Name","type":"string","desc":"Server identification"},"credential":{"index":1,"title":"Credential","type":"string","required":true,"input":{"type":"select","source":{"name":"credentials.fields","option":{"value":"id","title":"${username}@gmail.com"}}},"desc":"Credential to use when acessing the service"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.providers_schemas (
    id,
    vendor)
	values (
	LASTVAL(),
	'google');




insert into public.objects_schemas (
    pub_id,
	version,
    type,
    name,
    title,
    "desc",
    icon,
	schema,
    hidden,
    statefull,
    category, 
    fields,
    created_at,
    created_by_id)
	values (
		'60a272e0a93e2ebf957492db',
		1.0,
		2,
		'genericApiProvider',
        'Generic API Provider',
		'Generic API Provider',
		'plug',
		'{"version":1,"type":"provider","name":"genericApiProvider","vendor":"internal","category":"api","title":"Generic API Provider","desc":"Generic API Provider","icon":"plug","refs":{"credentials":{"type":"credential"}},"fields":{"name":{"index":0,"title":"Name","type":"string","desc":"API identification","required":true},"desc":{"index":1,"title":"Description","type":"string","desc":"API description"},"credential":{"index":2,"title":"Credential","type":"string","input":{"type":"select","source":{"name":"credentials.fields","option":{"value":"id","title":"name"}}},"desc":"Credential to use when acessing the API"},"baseUrl":{"index":3,"title":"Base URL","type":"string","required":true,"desc":"API base URL"}},"methods":{"toString":{"script":"fields.name"}}}',
		false,
		true,
		'api',
		'{"name":{"index":0,"title":"Name","type":"string","desc":"API identification","required":true},"desc":{"index":1,"title":"Description","type":"string","desc":"API description"},"credential":{"index":2,"title":"Credential","type":"string","input":{"type":"select","source":{"name":"credentials.fields","option":{"value":"id","title":"name"}}},"desc":"Credential to use when acessing the API"},"baseUrl":{"index":3,"title":"Base URL","type":"string","required":true,"desc":"API base URL"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.providers_schemas (
    id,
    vendor)
	values (
	LASTVAL(),
	'internal');	




insert into public.objects_schemas (
    pub_id,
	version,
    type,
    name,
    title,
    "desc",
    icon,
	schema,
    hidden,
    statefull,
    category, 
    fields,
    created_at,
    created_by_id)
	values (
		'60a2941c81171d3e0bd99219',
		1.0,
		2,
		'genericApiMethodProvider',
        'Generic API Method Provider',
		'Generic API Method Provider',
		'plug',
		'{"version":1,"type":"provider","name":"genericApiMethodProvider","vendor":"internal","category":"api","title":"Generic API Method Provider","desc":"Generic API method provider","icon":"running","refs":{"apis":{"type":"provider","filters":{"name":"genericApiProvider"}}},"fields":{"name":{"index":0,"title":"Name","type":"string","desc":"API method name","required":true},"desc":{"index":1,"title":"Description","type":"string","desc":"API method description"},"provider":{"index":2,"title":"API provider","type":"string","required":true,"input":{"type":"select","source":{"name":"apis.fields","option":{"value":"id","title":"name"}}},"desc":"API provider to use when calling the method"},"httpMethod":{"index":3,"title":"HTTP method","type":"string","required":true,"input":{"type":"select","options":[{"title":"GET","value":"GET"},{"title":"PUT","value":"PUT"},{"title":"POST","value":"POST"},{"title":"PATCH","value":"PATCH"},{"title":"DELETE","value":"DELETE"}]},"desc":"HTTP method type"},"url":{"index":4,"title":"URL","type":"string","required":true,"desc":"Absolute or relative URL of the method to call"},"reqContentType":{"index":5,"title":"Request content type","type":"string","required":true,"input":{"type":"select","options":[{"title":"FORM_DATA","value":"FORM_DATA"},{"title":"JSON","value":"JSON"},{"title":"XML","value":"XML"}]},"desc":"Type of the content sent in the request"},"reqFields":{"index":6,"title":"Parameters","type":"object","fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true}},"input":{"type":"list"},"desc":"Request parameters"},"headerFields":{"index":7,"title":"Header fields","type":"object","fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Text"},{"type":"script","title":"Script"},{"type":"function","title":"Function"}]}},"input":{"type":"list"},"desc":"Request header fields"},"bodyFields":{"index":8,"title":"Body fields","type":"object","fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Text"},{"type":"script","title":"Script"},{"type":"function","title":"Function"}]}},"input":{"type":"list"},"desc":"Request body fields"},"preReqVars":{"index":9,"title":"Pre-request variables","type":"object","fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Text"},{"type":"script","title":"Script"},{"type":"function","title":"Function"}]}},"input":{"type":"list"},"desc":"Pre request variables"},"postReqVars":{"index":10,"title":"Post-request variables","type":"object","fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Text"},{"type":"script","title":"Script"},{"type":"function","title":"Function"}]}},"input":{"type":"list"},"desc":"Post request variables"},"respFields":{"index":11,"title":"Response fields","type":"object","multiple":true,"fields":{"key":{"index":0,"width":4,"title":"Path","type":"string","required":true},"title":{"index":1,"width":4,"title":"Title","type":"string","required":true},"multiple":{"index":2,"width":2,"title":"Multiple?","type":"bool"}},"input":{"type":"list"},"desc":"Respose fields to be extracted"},"respContentType":{"index":12,"title":"Response content type","type":"string","required":true,"input":{"type":"select","options":[{"title":"JSON","value":"JSON"},{"title":"XML","value":"XML"}]},"desc":"Response content type"},"respIsArray":{"index":13,"title":"Is array?","type":"bool","desc":"Is response an array?"},"respNodePath":{"index":14,"title":"Node path","type":"string","desc":"Node path into response"}},"methods":{"toString":{"script":"fields.name"}}}',
		false,
		true,
		'api',
		'{"name":{"index":0,"title":"Name","type":"string","desc":"API method name","required":true},"desc":{"index":1,"title":"Description","type":"string","desc":"API method description"},"provider":{"index":2,"title":"API provider","type":"string","required":true,"input":{"type":"select","source":{"name":"apis.fields","option":{"value":"id","title":"name"}}},"desc":"API provider to use when calling the method"},"httpMethod":{"index":3,"title":"HTTP method","type":"string","required":true,"input":{"type":"select","options":[{"title":"GET","value":"GET"},{"title":"PUT","value":"PUT"},{"title":"POST","value":"POST"},{"title":"PATCH","value":"PATCH"},{"title":"DELETE","value":"DELETE"}]},"desc":"HTTP method type"},"url":{"index":4,"title":"URL","type":"string","required":true,"desc":"Absolute or relative URL of the method to call"},"reqContentType":{"index":5,"title":"Request content type","type":"string","required":true,"input":{"type":"select","options":[{"title":"FORM_DATA","value":"FORM_DATA"},{"title":"JSON","value":"JSON"},{"title":"XML","value":"XML"}]},"desc":"Type of the content sent in the request"},"reqFields":{"index":6,"title":"Parameters","type":"object","fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true}},"input":{"type":"list"},"desc":"Request parameters"},"headerFields":{"index":7,"title":"Header fields","type":"object","fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Text"},{"type":"script","title":"Script"},{"type":"function","title":"Function"}]}},"input":{"type":"list"},"desc":"Request header fields"},"bodyFields":{"index":8,"title":"Body fields","type":"object","fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Text"},{"type":"script","title":"Script"},{"type":"function","title":"Function"}]}},"input":{"type":"list"},"desc":"Request body fields"},"preReqVars":{"index":9,"title":"Pre-request variables","type":"object","fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Text"},{"type":"script","title":"Script"},{"type":"function","title":"Function"}]}},"input":{"type":"list"},"desc":"Pre request variables"},"postReqVars":{"index":10,"title":"Post-request variables","type":"object","fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Text"},{"type":"script","title":"Script"},{"type":"function","title":"Function"}]}},"input":{"type":"list"},"desc":"Post request variables"},"respFields":{"index":11,"title":"Response fields","type":"object","multiple":true,"fields":{"key":{"index":0,"width":4,"title":"Path","type":"string","required":true},"title":{"index":1,"width":4,"title":"Title","type":"string","required":true},"multiple":{"index":2,"width":2,"title":"Multiple?","type":"bool"}},"input":{"type":"list"},"desc":"Respose fields to be extracted"},"respContentType":{"index":12,"title":"Response content type","type":"string","required":true,"input":{"type":"select","options":[{"title":"JSON","value":"JSON"},{"title":"XML","value":"XML"}]},"desc":"Response content type"},"respIsArray":{"index":13,"title":"Is array?","type":"bool","desc":"Is response an array?"},"respNodePath":{"index":14,"title":"Node path","type":"string","desc":"Node path into response"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.providers_schemas (
    id,
    vendor)
	values (
	LASTVAL(),
	'internal');		