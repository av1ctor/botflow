import sanitizeHtml from 'sanitize-html';
import SHA256 from 'crypto-js/sha256';
import CryptoJS from 'crypto-js/crypto-js';

const defaultOptions = 
{
	allowedTags: ['span', 'h3', 'h4', 'h5', 'h6', 'blockquote', 'p', 'a', 'ul', 'ol', 'nl', 'li', 'b', 'i', 'strong', 'em', 'strike', 'code', 'hr', 'br', 'div', 
		'table', 'thead', 'caption', 'tbody', 'tr', 'th', 'td', 'pre', 'input', 'select', 'option', 'textarea', 'button', 'label', 'form', 'script'],
	allowedAttributes: 
	{
		'*': ['class', 'id', 'style'],
		'script': ['type'],
		'form': ['role', 'name', 'novalidate'],
		'a': ['href'],
		'label': ['*'],
		'input': ['*'],
		'select': ['*'],
		'option': ['*'],
		'textarea': ['*'],
		'button': ['*'],
	}
};

export default class SafeHtml
{
	static CLASS_NAME = 'rf-injected-script';

	static sanitize(html)
	{
		return sanitizeHtml(html, defaultOptions);
	}

	static injectScripts(scripts, className = SafeHtml.CLASS_NAME)
	{
		let ids = [];

		if(scripts)
		{
			for(let i = 0; i < scripts.length; i++)
			{
				let code = scripts[i].text;
				let id = SHA256(code).toString(CryptoJS.enc.Hex);
				
				let script = document.getElementById(id);
				if(script)
				{
					script.parentNode.removeChild(script);
				}

				script = document.createElement('script');
				script.classList.add(className);
				script.id = id;
				script.text = 'var robotikflow = window.robotikflow;\n' + code;

				let docScripts = document.getElementsByTagName('script');
				docScripts[0].parentNode.insertBefore(script, docScripts[0]);

				ids.push(id);
			}
		}

		return ids;
	}

	static async injectScriptFromUrl(url, className = SafeHtml.CLASS_NAME)
	{
		let res = await fetch(url);
		if(!res.ok)
		{
			return null;
		}
		
		let id = SHA256(url).toString(CryptoJS.enc.Hex);

		let script = document.getElementById(id);
		if(script)
		{
			script.parentNode.removeChild(script);
		}
		
		script = document.createElement('script');
		script.classList.add(className);
		script.id = id;
		
		let code = await res.text();
		script.text = 'var robotikflow = window.robotikflow;\n' + code;

		let docScripts = document.getElementsByTagName('script');
		docScripts[0].parentNode.insertBefore(script, docScripts[0]);

		return id;
	}

	static ejectScript(id)
	{
		if(id)
		{
			let el = document.getElementById(id);
			if(el)
			{
				el.parentNode.removeChild(el);
			}
		}
	}
}