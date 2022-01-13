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
		'608c2eeb8ecdf895dcdc2126',
		1.0,
		4,
		'storageTrigger',
        'Storage monitor',
		'Monitors a storage provider',
		'file',
		'{"version":1,"type":"trigger","name":"storageTrigger","options":0,"category":"storage","title":"Storage monitor","desc":"Monitors a storage provider","icon":"file","refs":{"providers":{"type":"provider","filters":{"category":"storage"}}},"fields":{"provider":{"index":0,"title":"Provider","type":"string","required":true,"input":{"type":"select","source":{"name":"providers.fields","option":{"value":"id","title":"title"}}},"desc":"Storage provider to monitor"},"path":{"index":1,"title":"Path","type":"string","required":true,"desc":"Path that will be monitored"},"op":{"index":2,"title":"Operation","type":"string","required":true,"input":{"type":"select","options":[{"value":"CREATED","title":"File created"},{"value":"UPDATED","title":"File updated"},{"value":"MOVED","title":"File moved"},{"value":"RENAMED","title":"File renamed"},{"value":"REMOVED","title":"File deleted"}]},"desc":"Operation that will be monitored"}},"operations":{"sync":{"out":{"multiple":true,"fields":{"name":{"type":"string","title":"Name"},"id":{"type":"string","title":"Id","hidden":true},"path":{"type":"string","title":"Path"},"extension":{"type":"string","title":"Extension","hidden":true},"mimeType":{"type":"string","title":"Mime type","hidden":true},"size":{"type":"number","title":"Size"},"createdAt":{"type":"datetime","title":"Created at"},"modifiedAt":{"type":"datetime","title":"Modified at"},"creator":{"type":"string","title":"Creator"},"url":{"type":"string","title":"URL","hidden":true},"files":{"type":"array","title":"Files","hidden":true}}}}}}',
		false,
		false,
		'storage',
		'{"provider":{"index":0,"title":"Provider","type":"string","required":true,"input":{"type":"select","source":{"name":"providers.fields","option":{"value":"id","title":"title"}}},"desc":"Storage provider to monitor"},"path":{"index":1,"title":"Path","type":"string","required":true,"desc":"Path that will be monitored"},"op":{"index":2,"title":"Operation","type":"string","required":true,"input":{"type":"select","options":[{"value":"CREATED","title":"File created"},{"value":"UPDATED","title":"File updated"},{"value":"MOVED","title":"File moved"},{"value":"RENAMED","title":"File renamed"},{"value":"REMOVED","title":"File deleted"}]},"desc":"Operation that will be monitored"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.triggers_schemas (
    id,
	options)
	values (
	LASTVAL(),
	0);




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
		'608c538b1d47e9078d56b06a',
		1.0,
		4,
		'emailTrigger',
        'E-mail monitor',
		'Monitors a e-mail provider',
		'envelope',
		'{"version":1,"type":"trigger","name":"emailTrigger","options":0,"category":"email","title":"E-mail monitor","desc":"Monitors a e-mail provider","icon":"envelope","refs":{"providers":{"type":"provider","filters":{"category":"email"}}},"fields":{"provider":{"index":0,"title":"Provider","type":"string","required":true,"input":{"type":"select","source":{"name":"providers.fields","option":{"value":"id","title":"title"}}},"desc":"E-mail provider to monitor"},"conditions":{"index":1,"title":"Conditions","type":"logtree","required":true,"fields":{"field":{"index":0,"title":"Field","type":"string","required":true,"input":{"type":"select","options":[{"value":"SENDER","title":"From"},{"value":"SUBJECT","title":"Subject"},{"value":"BODY","title":"Body"}]},"width":6,"desc":"E-mail field to verify"},"value":{"index":1,"title":"Value","type":"string","required":true,"width":6,"desc":"Value to check (Regular Expression)"}},"input":{"type":"logtree"},"desc":"Conditions"},"makeCopy":{"index":2,"title":"Store a copy?","type":"bool","desc":"Store a copy of the e-mail"}},"operations":{"sync":{"out":{"multiple":true,"fields":{"sender":{"type":"string","title":"Sender"},"subject":{"type":"string","title":"Subject"},"body":{"type":"string","title":"Body"},"files":{"type":"array","title":"Files","hidden":true}}}}}}',
		false,
		false,
		'email',
		'{"provider":{"index":0,"title":"Provider","type":"string","required":true,"input":{"type":"select","source":{"name":"providers.fields","option":{"value":"id","title":"title"}}},"desc":"E-mail provider to monitor"},"conditions":{"index":1,"title":"Conditions","type":"logtree","required":true,"fields":{"field":{"index":0,"title":"Field","type":"string","required":true,"input":{"type":"select","options":[{"value":"SENDER","title":"From"},{"value":"SUBJECT","title":"Subject"},{"value":"BODY","title":"Body"}]},"width":6,"desc":"E-mail field to verify"},"value":{"index":1,"title":"Value","type":"string","required":true,"width":6,"desc":"Value to check (Regular Expression)"}},"input":{"type":"logtree"},"desc":"Conditions"},"makeCopy":{"index":2,"title":"Store a copy?","type":"bool","desc":"Store a copy of the e-mail"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.triggers_schemas (
    id,
	options)
	values (
	LASTVAL(),
	0);



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
		'60a298f34ee9c48fc3bafe14',
		1.0,
		4,
		'genericApiMethodTrigger',
        'Generic API method trigger',
		'Invokes a generic API method',
		'plug',
		'{"version":1,"type":"trigger","name":"genericApiMethodTrigger","category":"api","title":"Generic API method trigger","desc":"Invokes a generic API method","icon":"plug","refs":{"providers":{"type":"provider","filters":{"name":"genericApiMethodProvider"}}},"fields":{"provider":{"index":0,"title":"Provider","type":"string","required":true,"input":{"type":"select","source":{"name":"providers.fields","option":{"value":"id","title":"name"}}},"desc":"API method provider to invoke"},"params":{"index":1,"title":"Parameters","type":"object","required":true,"fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true}},"input":{"type":"list"},"desc":"Request parameters"}},"operations":{"sync":{"out":{"multiple":true,"fields":null},"methods":{"getOutFields":{"script":"ref(''providers'', fields.provider).fields.respFields"}}}}}',
		false,
		false,
		'api',
		'{"provider":{"index":0,"title":"Provider","type":"string","required":true,"input":{"type":"select","source":{"name":"providers.fields","option":{"value":"id","title":"name"}}},"desc":"API method provider to invoke"},"params":{"index":1,"title":"Parameters","type":"object","required":true,"fields":{"key":{"index":0,"width":4,"title":"Name","type":"string","required":true},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true}},"input":{"type":"list"},"desc":"Request parameters"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.triggers_schemas (
    id,
	options)
	values (
	LASTVAL(),
	1);

