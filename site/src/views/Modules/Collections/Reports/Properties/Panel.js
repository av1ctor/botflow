import React, {useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../../components/Modal/ModalScrollable';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import Misc from '../../../../../libs/Misc';
import General from './General';
import Columns from './Columns';
import Filters from './Filters';
import Chart from './Chart';

const lut = {
	'general': General,
	'fields': Columns,
	'filters': Filters,
	'chart': Chart,
}; 

const Panel = observer(props =>
{
	const store = props.store;
	
	const [schema, setSchema] = useState({});
	const [report, setReport] = useState({});
	const [columns, setColumns] = useState([]);

	useEffect(() =>
	{
		if(!props.visible)
		{
			return;
		}
		
		const reports = store.collection.schemaObj.reports;
		
		const isCreation = !reports || props.index >= reports.length || reports[props.index].id !== props.id;
		
		const schema = !isCreation?
			store.collection.schemaObj:
			Misc.cloneObject(store.collection.schemaObj);

		const report = !isCreation?
			Object.assign({}, schema.reports[props.index]):
			store.generateReport(schema, props.id);

		setSchema(schema);
		setReport(report);
		setColumns(CollectionUtil.transformColumns(schema, schema.columns));
	}, [store.collection, props.visible]);

	if(!props.visible)
	{
		return null;
	}

	const Compo = lut[props.compo];

	return (
		<ModalScrollable 
			isOpen={props.visible}
			onHide={props.onHide}
			lockFocus
			icon={props.icon} 
			title={`Relatório "${report.name}" > Propriedades > ${props.title}`} 
		>
			<Compo
				{...props}
				schema={schema}
				report={report}
				columns={columns}
			/>
		</ModalScrollable>
	);
});

Panel.propTypes = 
{
	container: PropTypes.object,
	store: PropTypes.object,
	visible: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
	index: PropTypes.number,
	id: PropTypes.string,
};

export default Panel;