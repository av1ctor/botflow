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
		'608e8ed13ff0fc29c84d2c3a',
		1.0,
		3,
		'insertItemActivity',
        'Insert item',
		'Insert a new item into the collection',
		'table',
		'{"version":1,"type":"activity","name":"insertItemActivity","category":"collection","dir":"out","title":"Item insert","desc":"Insert a new item into the collection","icon":"table","fields":{"fields":{"index":0,"title":"Fields","type":"object","fields":{"key":{"index":0,"width":4,"title":"Field","type":"string","required":true,"input":{"type":"select","source":{"name":"collection.fields","option":{"title":"label","value":"id"}}}},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Value"},{"type":"field","title":"Field","source":{"name":"in.fields","option":{"title":"title","value":"key"}}},{"type":"function","title":"Function"},{"type":"script","title":"Script"}]}},"input":{"type":"list"},"desc":"Fields to insert"}}}',
		false,
		false,
		'collection',
		'{"fields":{"index":0,"title":"Fields","type":"object","fields":{"key":{"index":0,"width":4,"title":"Field","type":"string","required":true,"input":{"type":"select","source":{"name":"collection.fields","option":{"title":"label","value":"id"}}}},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Value"},{"type":"field","title":"Field","source":{"name":"in.fields","option":{"title":"title","value":"key"}}},{"type":"function","title":"Function"},{"type":"script","title":"Script"}]}},"input":{"type":"list"},"desc":"Fields to insert"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.activities_schemas (
    id,
	dir)
	values (
	LASTVAL(),
	1);


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
		'609437424c48779ec1125374',
		1.0,
		3,
		'updateCurrentItemActivity',
        'Update current item',
		'Update the current item in the collection',
		'pen',
		'{"version":1,"type":"activity","name":"updateCurrentItemActivity","category":"collection","dir":"out","title":"Update current item","desc":"Update the current item in the collection","icon":"table","fields":{"fields":{"index":0,"title":"Fields","type":"object","fields":{"key":{"index":0,"width":4,"title":"Field","type":"string","required":true,"input":{"type":"select","source":{"name":"collection.fields","option":{"title":"label","value":"id"}}}},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Value"},{"type":"function","title":"Function"},{"type":"script","title":"Script"}]}},"input":{"type":"list"},"desc":"Fields to update"}}}',
		false,
		false,
		'collection',
		'{"fields":{"index":0,"title":"Fields","type":"object","fields":{"key":{"index":0,"width":4,"title":"Field","type":"string","required":true,"input":{"type":"select","source":{"name":"collection.fields","option":{"title":"label","value":"id"}}}},"value":{"index":1,"width":6,"title":"Value","type":"string","required":true,"inputs":[{"type":"text","title":"Value"},{"type":"function","title":"Function"},{"type":"script","title":"Script"}]}},"input":{"type":"list"},"desc":"Fields to update"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.activities_schemas (
    id,
	dir)
	values (
	LASTVAL(),
	1);



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
		'60953ad732ed587fc182f07d',
		1.0,
		3,
		'sendEmailActivity',
        'Send e-mail',
		'Sends an e-mail using a configured provider',
		'envelope',
		'{"version":1,"type":"activity","name":"sendEmailActivity","category":"email","dir":"out","title":"Send e-mail","desc":"Sends an e-mail using a configured provider","icon":"envelope","refs":{"providers":{"type":"provider","filters":{"category":"email"}}},"fields":{"provider":{"index":0,"title":"Provider","type":"string","required":true,"input":{"type":"select","source":{"name":"providers.fields","option":{"value":"id","title":"title"}}},"desc":"Name of the configured e-mail provider"},"from":{"index":1,"title":"From","type":"string","placeholder":"from@me.com","input":{"type":"email","validate":[{"type":"email","err":"Senders e-mail address is invalid"}]},"desc":"Address of the sender"},"to":{"index":2,"title":"To","type":"string","required":true,"placeholder":"to@them.com","input":{"type":"email","validate":[{"type":"email","err":"Receivers e-mail address is invalid"}]},"desc":"Address of the receiver"},"subject":{"index":3,"title":"Subject","type":"string","required":true,"input":{"type":"text"},"desc":"Subject of the e-email"},"body":{"index":4,"title":"Body","type":"string","input":{"type":"textarea","title":"Text","rows":5},"desc":"Body of the e-mail"}}}',
		false,
		false,
		'email',
		'{"provider":{"index":0,"title":"Provider","type":"string","required":true,"input":{"type":"select","source":{"name":"providers.fields","option":{"value":"id","title":"title"}}},"desc":"Name of the configured e-mail provider"},"from":{"index":1,"title":"From","type":"string","placeholder":"from@me.com","input":{"type":"email","validate":[{"type":"email","err":"Senders e-mail address is invalid"}]},"desc":"Address of the sender"},"to":{"index":2,"title":"To","type":"string","required":true,"placeholder":"to@them.com","input":{"type":"email","validate":[{"type":"email","err":"Receivers e-mail address is invalid"}]},"desc":"Address of the receiver"},"subject":{"index":3,"title":"Subject","type":"string","required":true,"input":{"type":"text"},"desc":"Subject of the e-email"},"body":{"index":4,"title":"Body","type":"string","input":{"type":"textarea","title":"Text","rows":5},"desc":"Body of the e-mail"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.activities_schemas (
    id,
	dir)
	values (
	LASTVAL(),
	1);
