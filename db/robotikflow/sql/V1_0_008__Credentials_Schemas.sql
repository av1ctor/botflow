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
		'6089a350816b9c5823649558',
		1.0,
		1,
		'googleOAuth2',
        'Google OAuth 2',
		'Google OAuth 2 Credential',
		'google',
		'{"version":1,"type":"credential","name":"googleOAuth2","vendor":"google","mode":"oauth2","oauth2":"credentials","title":"Google OAuth 2","desc":"Google OAuth 2 Credential","icon":"google","fields":{"authUrl":{"index":0,"title":"Authorization URL","type":"string","required":true,"default":"https://accounts.google.com/o/oauth2/v2/auth","disabled":true,"desc":"Google OAuth 2 authorization URL"},"tokenUrl":{"index":1,"title":"Token URL","type":"string","required":true,"default":"https://accounts.google.com/o/oauth2/v2/token","disabled":true,"desc":"Google OAuth 2 token URL"},"scopes":{"index":2,"title":"Scopes","type":"string","required":true,"default":"https://www.googleapis.com/auth/gmail.readonly https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/drive https://www.googleapis.com/auth/drive.metadata https://www.googleapis.com/auth/drive.activity.readonly","disabled":true,"desc":"Google OAuth 2 scopes"},"clientId":{"index":3,"title":"Client id","type":"string","desc":"Google client id (leave empty to use Robotiflow client id)"},"clientSecret":{"index":4,"title":"Client secret","type":"string","input":{"type":"password"},"desc":"Google client secret (leave empty to use Robotiflow client secret)"},"username":{"index":5,"title":"Username","type":"string","required":true,"validate":[{"type":"min","value":3,"err":"Username length must be at least 3 chars long"},{"type":"max","value":32,"err":"Username length must be at most 32 chars long"}],"desc":"Google user name"},"authorizationCode":{"index":6,"title":"Authorization code","type":"string","disabled":true,"hidden":true},"accessToken":{"index":7,"title":"Access token","type":"string","disabled":true,"desc":"OAuth 2 access token"},"refreshToken":{"index":8,"title":"Refresh token","type":"string","disabled":true,"desc":"OAuth 2 refresh token"},"tokenExpiration":{"index":9,"title":"Token expiration","type":"datetime","disabled":true,"desc":"OAuth 2 token expiration date"}},"methods":{"toString":{"script":"fields.username + ''@google''"}}}',
		false,
		true,
		'oauth2',
		'{"authUrl":{"index":0,"title":"Authorization URL","type":"string","required":true,"default":"https://accounts.google.com/o/oauth2/v2/auth","disabled":true,"desc":"Google OAuth 2 authorization URL"},"tokenUrl":{"index":1,"title":"Token URL","type":"string","required":true,"default":"https://accounts.google.com/o/oauth2/v2/token","disabled":true,"desc":"Google OAuth 2 token URL"},"scopes":{"index":2,"title":"Scopes","type":"string","required":true,"default":"https://www.googleapis.com/auth/gmail.readonly https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/drive https://www.googleapis.com/auth/drive.metadata https://www.googleapis.com/auth/drive.activity.readonly","disabled":true,"desc":"Google OAuth 2 scopes"},"clientId":{"index":3,"title":"Client id","type":"string","desc":"Google client id (leave empty to use Robotiflow client id)"},"clientSecret":{"index":4,"title":"Client secret","type":"string","input":{"type":"password"},"desc":"Google client secret (leave empty to use Robotiflow client secret)"},"username":{"index":5,"title":"Username","type":"string","required":true,"validate":[{"type":"min","value":3,"err":"Username length must be at least 3 chars long"},{"type":"max","value":32,"err":"Username length must be at most 32 chars long"}],"desc":"Google user name"},"authorizationCode":{"index":6,"title":"Authorization code","type":"string","disabled":true,"hidden":true},"accessToken":{"index":7,"title":"Access token","type":"string","disabled":true,"desc":"OAuth 2 access token"},"refreshToken":{"index":8,"title":"Refresh token","type":"string","disabled":true,"desc":"OAuth 2 refresh token"},"tokenExpiration":{"index":9,"title":"Token expiration","type":"datetime","disabled":true,"desc":"OAuth 2 token expiration date"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.credentials_schemas (
    id,
    vendor,
	mode)
	values (
	LASTVAL(),
	'google',
	3);



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
		'60a273e061deafbce032f37e',
		1.0,
		1,
		'userPwdCredential',
        'Username/password',
		'Username/password Credential',
		'google',
		'{"version":1,"type":"credential","name":"userPwdCredential","vendor":"internal","mode":"user_pwd","category":"user_pwd","title":"Username/password","desc":"Username/password Credential","icon":"user","fields":{"name":{"index":0,"title":"Id","type":"string","required":true,"desc":"Credential identification"},"username":{"index":2,"title":"Username","type":"string","required":true,"desc":"User name"},"password":{"index":3,"title":"Password","type":"string","input":{"type":"password"},"required":true,"desc":"User password"}},"methods":{"toString":{"script":"fields.name + ''(userPwd)''"}}}',
		false,
		true,
		'user_pwd',
		'{"name":{"index":0,"title":"Id","type":"string","required":true,"desc":"Credential identification"},"username":{"index":2,"title":"Username","type":"string","required":true,"desc":"User name"},"password":{"index":3,"title":"Password","type":"string","input":{"type":"password"},"required":true,"desc":"User password"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.credentials_schemas (
    id,
    vendor,
	mode)
	values (
	LASTVAL(),
	'internal',
	4);	


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
		'60a276a9c4452ae46160073f',
		1.0,
		1,
		'keySecretCredential',
        'API key',
		'API key Credential',
		'plug',
		'{"version":1,"type":"credential","name":"keySecretCredential","vendor":"internal","mode":"api_key","category":"api_key","title":"API key","desc":"Generic API key/secret credential","icon":"plug","fields":{"name":{"index":0,"title":"Name","type":"string","required":true,"desc":"Identification"},"key":{"index":1,"title":"Client key","type":"string","required":true,"desc":"API client key"},"secret":{"index":2,"title":"Client secret","type":"string","input":{"type":"password"},"desc":"API secret"}},"methods":{"toString":{"script":"fields.name + ''(apiKey)''"}}}',
		false,
		false,
		'api_key',
		'{"name":{"index":0,"title":"Name","type":"string","required":true,"desc":"Identification"},"key":{"index":1,"title":"Client key","type":"string","required":true,"desc":"API client key"},"secret":{"index":2,"title":"Client secret","type":"string","input":{"type":"password"},"desc":"API secret"}}',
		CURRENT_TIMESTAMP,
		1
	);

insert into public.credentials_schemas (
    id,
    vendor,
	mode)
	values (
	LASTVAL(),
	'internal',
	1);		