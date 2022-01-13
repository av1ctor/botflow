import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import saveAs from 'file-saver';
import FloatingButtonBar from '../../../../components/Bar/FloatingButtonBar';
import CollectionUtil from '../../../../libs/CollectionUtil';
import Misc from '../../../../libs/Misc';

const Manager = observer(props =>
{
	const store = props.store;

	const [toCopy, setToCopy] = useState([]);

	useEffect(() =>
	{
		setToCopy([]);
	}, [props.view]);

	const filterSelected = () =>
	{
		return Misc.mapValues(props.rows).filter(row => row.selected);
	};

	const handleCopy = useCallback(async () =>
	{
		const selected = filterSelected();

		setToCopy(selected);

		for(let i = 0; i < selected.length; i++)
		{
			const row = selected[i];
			await props.container.toggleSelectRow(row);
		}

		props.container.showMessage(`${selected.length} itens prontos para serem colados!`, 'info');
	}, [props.rows, toCopy]);

	const handlePaste = useCallback(async () =>
	{
		if(toCopy.length === 0)
		{
			props.container.showMessage('Nenhum item copiado', 'error');
			return;
		}

		const destines = filterSelected();
		if(destines.length !== 1)
		{
			props.container.showMessage('Selecione apenas um item para onde colar', 'error');
			return;
		}

		const destine = destines[0];

		const idColumn = CollectionUtil.ID_NAME;
		for(let i = 0; i < toCopy.length; i++)
		{
			const row = toCopy[i];

			await props.container.createRow({
				...destine, 
				appended: {
					item: {
						...row.item, 
						[idColumn]: undefined
					}
				}
			}, {isPasted: true});
		}

		props.container.showMessage(`${toCopy.length} itens colados!`, 'success');
	}, [props.rows, toCopy]);

	const del = async (toDelete) =>
	{
		props.container.deleteRows(toDelete);
	};

	const handleDelete = useCallback(() =>
	{
		const toDelete = filterSelected();
		props.container.showUserConfirmation(() => del(toDelete), null, `Apagar todos os ${toDelete.length} itens selecionados?`);
	}, [props.rows]);

	const handleExport = useCallback(() =>
	{
		const collection = store.collection;
		const references = store.references;
		const schema = collection.schemaObj;
		const fields = Object.entries(props.view.fields)
			.map(([key, field]) => ({key, ...field}))
			.sort((a, b) => (a.index||0) - (b.index||0));
		const toExport = filterSelected();
		
		const csv = [];
		const header = [];
		for(let i = 0; i < fields.length; i++)
		{
			const field = fields[i];
			header.push(`"${field.key}"`);
		}
		csv.push(header.join('\t'));

		for(let i = 0; i < toExport.length; i++)
		{
			const cols = [];
			const item = toExport[i].item;
			for(let j = 0; j < fields.length; j++)
			{
				const field = fields[j];
				const column = CollectionUtil.getColumn(schema, field.key, references);
				const klass = column && column.class? 
					CollectionUtil.getClassByName(schema, column.class): 
					null;
	
				const value = CollectionUtil.getColumnValue(column, item, field.key);
				let valueFormatted = CollectionUtil.getValueFormatted(value, column, klass, false);
				
				switch(column.type)
				{
				case 'number':
					break;
				
				default:
					valueFormatted = `"${valueFormatted}"`;
					break;
				}

				cols.push(valueFormatted);
			}

			csv.push(cols.join('\t'));
		}
		
		const blob = new Blob(['\uFEFF' + csv.join('\r\n')], {type: 'text/csv;charset=utf-8'});
		saveAs(blob, `${collection.name}-${props.view.name}.csv`);
	}, [props.rows]);

	const handleAdmin = useCallback(() =>
	{
		
	}, [props.rows]);

	return (
		<FloatingButtonBar
			visible={props.selectedCnt > 0}>
			<div onClick={handleCopy}>
				<div><i className="la la-copy fbb-icon"/></div>
				<div>Copiar</div>
			</div>
			<div onClick={handlePaste}>
				<i className="la la-paste fbb-icon"/>
				<div>Colar</div>
			</div>
			<div onClick={handleDelete}>
				<i className="la la-trash fbb-icon"/>
				<div>Apagar</div>
			</div>
			<div onClick={handleExport}>
				<i className="la la-cloud-download fbb-icon"/>
				<div>Exportar</div>
			</div>
			{props.container.isCreator() &&
				<div onClick={handleAdmin}>
					<i className="la la-cog fbb-icon"/>
					<div>Administrar</div>
				</div>
			}
		</FloatingButtonBar>
	);
});

Manager.propTypes = {
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	view: PropTypes.object.isRequired,
	selectedCnt: PropTypes.number.isRequired,
	rows: PropTypes.any.isRequired
};

export default Manager;
