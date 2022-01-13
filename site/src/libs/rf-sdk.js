if(!window.robotikflow)
{
	var TemplateEngine = function(html, options) 
	{
		var re = /{{([^}]+)?}}/g;
		var reExp = /(^( )?(if|for|else|switch|case|break|{|}))(.*)?/g;
		var code = 'var r=[];\n';
		var cursor = 0;
		var match = null;
		
		var add = function(line, js) 
		{
			js? (code += line.match(reExp) ? line + '\n' : 'r.push(' + line + ');\n') :
				(code += line !== '' ? 'r.push("' + line.replace(/"/g, '\\"') + '");\n' : '');
			return add;
		};
		
		while((match = re.exec(html))) 
		{
			add(html.slice(cursor, match.index))(match[1], true);
			cursor = match.index + match[0].length;
		}
		
		add(html.substr(cursor, html.length - cursor));
		code += 'return r.join("");';
		
		return new Function(' with (this) { ' + code.replace(/[\r\t\n]/g, '') + '}').apply(options);
	};

	(function()
	{
		var api = null;
		var helper = null;
		var listeners = {
			loaded: [], 
			validated: []
		};

		var __emptyListeners = function()
		{
			return {
				loaded: [], 
				validated: []
			};	
		};

		var __domLoaded = function()
		{
			while(listeners.loaded.length > 0)
			{
				try
				{
					listeners.loaded.shift()();
				}
				catch(ex)
				{
					helper.showMessage('Erro na callback para o evento "carregado": ' + ex, 'error');
				}
			}
		};

		var config = function(options)
		{
			api = options.api;
			helper = options.helper;
			listeners = __emptyListeners();
		};

		var quando = function(eventName, callback)
		{
			switch(eventName)
			{
			case 'carregado':
				if(document.readyState === 'loading')
				{
					listeners.loaded.push(callback);
				}
				else
				{
					try
					{
						callback();
					}
					catch(ex)
					{
						helper.showMessage('Erro na callback para o evento "carregado": ' + ex, 'error');
					}
				}
				break;
			
			case 'validado':
				listeners.validated.push(callback);
				break;
			
			default:
				helper.showMessage('Evento desconhecido:' + eventName, 'error');
				break;
			}
		};

		var reportarErro = function(errors, form, varName, msg)
		{
			try
			{
				var element = form.elements.namedItem(varName);
				errors.push({element: element, name: varName, msg: msg});
				element.setCustomValidity(msg);
			}
			catch(ex)
			{
				console.error(ex);
			}
		};

		var formularioService =
		{
			carregar: function(form, isDisabled)
			{
				listeners = __emptyListeners();
	
				for(let i = 0; i < form.elements.length; i++)
				{
					let elm = form.elements[i];
					if(elm.getAttribute('var-formato'))
					{
						helper.maskInput(elm, elm.getAttribute('var-formato'));
					}
				}
	
				for(let [key, value] of Object.entries(window.robotikflow.variaveis)) 
				{
					for(let i = 0; i < form.elements.length; i++)
					{
						let elm = form.elements[i];
						if(elm.getAttribute('name') === key || elm.getAttribute('var-name') === key)
						{
							elm.value = value;
							if(isDisabled)
							{
								elm.disabled = true;
							}
						}
					}
				}
			},
	
			validar: function(form)
			{
				var res = {vars: {}, erros: []};
	
				for(var i = 0; i < form.elements.length; i++)
				{
					var err = null;
					
					var element = form.elements[i];
					element.setCustomValidity('');
	
					var varName = element.getAttribute('name') || element.getAttribute('var-name');
					if(varName)
					{
						var varType = element.getAttribute('var-type');
						var varValue = element.value;
						if(varType !== null && varType !== undefined)
						{
							switch(varType.toLowerCase())
							{
							case 'short':
							case 'integer':
							case 'long':
								break;
							case 'float':
							case 'double':
								break;
							case 'string':
								break;
							case 'boolean':
								if(varValue.toLowerCase() !== 'true' && varValue.toLowerCase() !== 'false')
								{
									err = 'Tipo boleano só pode conter valores true ou false';
								}
								break;
							
							default:
								err = `Tipo desconhecido de variável: ${varType}`;
							}
						}
	
						var required = element.getAttribute('required');
						if(required !== null && required !== 'false')
						{
							if(varValue === null || varValue === undefined || varValue === '')
							{
								err = 'Campo de preenchimento obrigatório';
							}
						}
	
						if(err)
						{
							res.erros.push({element: element, name: varName, msg: err});
							element.setCustomValidity(err);
						}
	
						res.variaveis[varName] = {
							value: varValue,
							type: varType
						};
					}
				}
	
				for(var j = listeners.validated.length - 1; j >= 0; j--)
				{
					try
					{
						listeners.validated[j](form, res.variaveis, res.erros);
					}
					catch(ex)
					{
						helper.showMessage('Erro na callback para o evento "validado": ' + ex, 'error');
					}
				}
	
				form.classList.add('was-validated');
	
				return res;
			},

			enviar: function(event, form)
			{
				helper.complete(event, form);
			}
		};

		var documentoService =
		{
			gerarUrlDeDownload: async function (document)
			{
				try
				{
					let res = await api.get(`docs/${document.id}?validade=3600`);
					if(res.errors !== null)
					{
						throw res.errors;
					}
						
					return res.data.url;
				}
				catch(ex)
				{
					helper.showMessage('Geração de URL falhou: ' + ex, 'error');
					return '';
				}
			},

			prepararLinkParaDownload: function(document, url, anchor)
			{
				try
				{
					anchor.href = `docs/${document.id}`;
					anchor.onclick = function(e) 
					{
						e.preventDefault();

						var xhr = new XMLHttpRequest();
						xhr.open('GET', url);
						xhr.responseType = 'blob';
						xhr.onload = function()
						{
							helper.saveAs(xhr.response, document.name);
						};
						
						xhr.onerror = function()
						{
							helper.showMessage('Download do arquivo falhou: ' + xhr.statusText, 'error');
						};

						xhr.send();
					};
				}
				catch(ex)
				{
					helper.showMessage('Download do arquivo falhou: ' + ex);
				}
			}
		};

		var collectionService =
		{
			findOne: async function (collection, filtros)
			{
				try
				{
					let filtrosStr = Object.entries(filtros).map(([k,v]) => `"${k}":"${v}"`).join(',');
					let res = await api.get(`collections/name/${collection}/items?filters=${filtrosStr}`);
					if(res.errors !== null)
					{
						helper.showMessage('Procura por item de coleção falhou', 'error');
						return null;
					}
						
					return res.data;
				}
				catch(ex)
				{
					helper.showMessage('Procura por item de coleção falhou: ' + ex, 'error');
					return null;
				}
			},

			findAll: async function (collection, filtros = {})
			{
				try
				{
					let filtrosStr = Object.entries(filtros).map(([k,v]) => `"${k}":"${v}"`).join(',');
					let res = await api.get(`collections/name/${collection}/items?filters=${filtrosStr}`);
					if(res.errors !== null)
					{
						helper.showMessage('Procura por itens de coleção falhou', 'error');
						return null;
					}
						
					return res.data;
				}
				catch(ex)
				{
					helper.showMessage('Procura por itens de coleção falhou: ' + ex, 'error');
					return null;
				}
			},

			inserir: async function(collection, variaveis)
			{
				try
				{
					let res = await api.post(`collections/name/${collection}/items`, {vars: variaveis});
					if(res.errors !== null)
					{
						throw res.errors;
					}
						
					return res.data;
				}
				catch(ex)
				{
					helper.showMessage('Criação de item de coleção falhou: ' + ex, 'error');
					return null;
				}
			}
		};

		var paginaService =
		{
			definirContainer: function(container)
			{
				window.robotikflow.container = container;
			},

			injetarMenu: function(element)
			{
				try
				{
					helper.injectMenu(element);
				}
				catch(ex)
				{
					helper.showMessage('Injeção do menu falhou: ' + ex, 'error');
				}
			},

			compilar: function(variaveis)
			{
				try
				{
					window.robotikflow.container.innerHTML = TemplateEngine(window.robotikflow.container.innerHTML, variaveis);
				}
				catch(ex)
				{
					helper.showMessage('Compilação da página falhou: ' + ex, 'error');
				}
			}
		};

		var workflowService =
		{
			iniciar: async function(workflow, variaveis)
			{
				try
				{
					let res = await api.post(`workflows/name/${workflow}/iniciar`, {variaveis: variaveis});
					if(res.errors !== null)
					{
						throw res.errors;
					}
						
					return res.data;
				}
				catch(ex)
				{
					helper.showMessage('Inicialização de workflow falhou: ' + ex, 'error');
					return null;
				}
			}
		};

		document.addEventListener('DOMContentLoaded', __domLoaded);

		window.robotikflow = 
		{
			container: null,
			variaveis: {},
			config: config,
			quando,
			reportarErro,
			formularioService,
			documentoService,
			colecaoService,
			paginaService,
			workflowService
		};
	
	})();
}