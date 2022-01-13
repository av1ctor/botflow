import PaletteProvider from 'bpmn-js/lib/features/palette/PaletteProvider';
import ReplaceMenuProvider from 'bpmn-js/lib/features/popup-menu/ReplaceMenuProvider';

const paletteEntriesToRemove = new Array(
	'space-tool',
	'global-connect-tool',
	'create.intermediate-event', 
	'create.data-object', 
	'create.data-store', 
	'create.subprocess-expanded', 
	'create.group');


const _getPaletteEntries = PaletteProvider.prototype.getPaletteEntries;
PaletteProvider.prototype.getPaletteEntries = function(element)
{
	const entries = _getPaletteEntries.call(this, element);
	for(let entry of paletteEntriesToRemove)
	{
		delete entries[entry];
	}
	return entries; 
};

const menuEntriesToAllow = new Array(
	'replace-with-none-start',
	'replace-with-none-end',
	'replace-with-exclusive-gateway',
	'replace-with-parallel-gateway',
	'replace-with-user-task',
	'replace-with-service-task',
	'replace-with-sequence-flow',
	'replace-with-default-flow',
	'replace-with-conditional-flow',
);

const _getEntries = ReplaceMenuProvider.prototype.getEntries;
ReplaceMenuProvider.prototype.getEntries = function(element) 
{
	const entries = _getEntries.call(this, element);
	
	let i = 0;
	while(i < entries.length)
	{
		const index = menuEntriesToAllow.indexOf(entries[i].id);
		if(index === -1)
		{
			entries.splice(i, 1);
		}
		else
		{
			++i;
		}
	}

	return entries;
};

ReplaceMenuProvider.prototype._getLoopEntries = function(element) {
	return [];
};

