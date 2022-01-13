import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Input, Row, Col, Button, Container, InputGroup, InputGroupAddon, Badge} from 'reactstrap';
import FilterValueInput from '../../../../components/Common/Collection/View/FilterValueInput';
import CollectionUtil from '../../../../libs/CollectionUtil';
import LogicalTree from '../../../../components/Tree/LogicalTree';
import SimpleCollapse from '../../../../components/Collapse/SimpleCollapse';
import ActivitiesList from '../Activities/ActivitiesList';
import IntegrationForm from '../Integrations/IntegrationForm';

// eslint-disable-next-line react/prop-types
const Field = ({children, label}) =>
	<div className="row">
		<div className="col-12">
			<div className="pt-2">{label}</div>
			{children}
		</div>
	</div>;

const getConditionPrototype = () =>
{
	return {
		type: 'COLUMN',
		column: {
			name: '',
			op: '',
			value: null
		},
		automation: {
			name: ''
		}
	};
};

const Properties = observer(props =>
{
	const store = props.store;

	const [columns, setColumns] = useState([]);

	useEffect(() =>
	{
		setColumns(transformColumns(store.collection));
	}, []);

	useEffect(() =>
	{
		setColumns(transformColumns(store.collection));
	}, [store.collection]);

	const transformColumns = (collection) =>
	{
		const schema = collection.schemaObj;
		return CollectionUtil.transformColumns(schema, schema.columns);
	};

	const renderColumn = (item, id) =>
	{
		const renderType = () =>
		{
			const type = item.column.value?
				(item.column.value.script !== undefined? 
					'script': 
					item.column.value.column !== undefined? 
						'column': 
						'value'):
				'value';

			const value = type === 'script'? 
				item.column.value.script: 
				type === 'column'? 
					item.column.value.column: 
					(item.column.value? 
						item.column.value.value !== undefined?
							item.column.value.value: 
							item.column.value:
						null);

			const op = item.column.op;

			const colType = (column && column.type) || 'string';

			let compo = null;

			switch(type)
			{
			case 'value':
			{
				const values = FilterValueInput.toInput(colType, op, value || '');
				compo = 
					FilterValueInput.renderInput({
						disabled: !column || !op,
						className: 'flex-grow-1',
						column: column,
						type: colType,
						scalarType: colScalarType,
						op: op,
						values: values,
						onChange: (e, name) => {
							const i = name === 'values[0]'? 0: 1;
							values[i] = e.target.value;
							props.onChange({
								target: {
									name: `condition[${id}].column.value`,
									value: {value: FilterValueInput.fromInput(colType, column.subtype, op, values)}
								}
							});
						}
					});
				break;
			}
	
			case 'column':
				compo = 
					<Input
						disabled={!column || !op || op === 'isnull' || op === 'notnull'}
						type="select"
						value={value || ''}
						onChange={(e) =>
							props.onChange({
								target: {
									name: `condition[${id}].column.value`,
									value: {column: e.target.value}
								}
							})						
						}>
						{renderColumns()}
					</Input>;
				break;
	
			case 'script':
				compo = 
					<Input
						disabled={!column || !op || op === 'isnull' || op === 'notnull'}
						type="text"
						value={value || ''}
						onChange={(e) =>
							props.onChange({
								target: {
									name: `condition[${id}].column.value`,
									value: {script: e.target.value}
								}
							})						
						}>
					</Input>;
				break;
			}
	
			return (
				<InputGroup className="d-flex">
					<InputGroupAddon addonType="prepend" className="flex-grow-0">
						<Input 
							disabled={!column || !op || op === 'isnull' || op === 'notnull'}
							type="select" 
							value={type} 
							onChange={(e) => props.onChange({
								target: {
									name: `condition[${id}].column.value`,
									value: {[e.target.value]: value} 
								}
							})}>
							{renderTypes()}
						</Input>
					</InputGroupAddon>
					{compo}
				</InputGroup>
			);
		};

		const renderOps = (type) =>
			CollectionUtil.getFilterOps(type)
				.map((opt, index) => <option key={index} value={opt.value}>{`${opt.symbol} ${opt.label}`}</option>);

		const renderTypes = () =>
			Object.entries(CollectionUtil.getFlowConditionValueTypes())
				.map(([key, opt], index) => <option key={index} value={key}>{opt.name}</option>);

		const renderColumns = () =>
			[{name: '', label: ''}].concat(columns)
				.map((col, index) => <option key={index} value={col.name}>{col.label}</option>);
		
		const getColumnScalarType = (col) => (col && (col.type !== 'array'? col.type : col.subtype)) || 'string';				

		const schema = store.collection.schemaObj;

		const column = item.column.name?
			CollectionUtil.getColumn(schema, item.column.name, null):
			{};
		
		const colScalarType = !column.ref? 
			getColumnScalarType(column):
			'id';

		return (
			<>
				<Field label="Campo">
					<Input 
						type="select" 
						name={`condition[${id}].column.name`}
						value={item.column.name || ''} 
						onChange={props.onChange}>
						{renderColumns()}
					</Input>
				</Field>
				<Field label="Operador">
					<Input
						disabled={!column}
						type="select"
						name={`condition[${id}].column.op`}
						value={item.column.op || ''}
						onChange={props.onChange}>
						{renderOps(colScalarType)}
					</Input>
				</Field>
				<Field label="Tipo/Valor">
					{renderType()}
				</Field>
			</>
		);
	};

	const renderAutomation = (item, id) =>
	{
		const renderAutomationOptions = () => 
		{
			return [{id: '', desc: ''}].concat(props.automations).map((auto, index) => 
				<option key={index} value={auto.id}>{auto.desc}</option>);
		};
		
		return (
			<Field label="Automação">
				<Input 
					type="select" 
					name={`condition[${id}].automation.name`}
					value={(item.automation && item.automation.name) || ''} 
					onChange={props.onChange}>
					{renderAutomationOptions()}
				</Input>
			</Field>
		);
	};

	const renderCondition = (item, id) => 
	{
		const renderTypes = () =>
			Object.entries(CollectionUtil.getFlowConditionTypes())
				.map(([key, opt], index) => <option key={index} value={key}>{opt.label}</option>);

		return (
			<>
				<Field label="Tipo">
					<Input
						type="select"
						name={`condition[${id}].type`}
						value={item.type}
						onChange={props.onChange}>
						{renderTypes()}
					</Input>
				</Field>
				{item.type === 'COLUMN' &&
					renderColumn(item, id)
				}
				{item.type === 'AUTOMATION' &&
					renderAutomation(item, id)
				}
			</>
		);
	};

	const renderConditionPreview = (item) => 
	{
		const findAutomation = (id) => 
			props.automations.find(aut => aut.id === id) || {};

		const findField = (name) =>
			columns.find(col => col.name === name) || {};
		
		const findOp = (op) =>
			CollectionUtil.getFilterOps().find(oper => oper.value === op) || {};

		const getValue = (val) =>
			val.script || val.column || val.value || '';

		if(item.type === 'AUTOMATION')
		{
			return (
				<div><b>{findAutomation(item.automation.name).desc || ''}</b> realizada</div>
			);
		}
		else
		{
			const value = getValue(item.column.value || {});
			return (
				<div><b>{findField(item.column.name).label || ''}</b> <Badge pill color="dark">{findOp(item.column.op).symbol || ''}</Badge> <input type="text" value={value} readOnly disabled /></div>
			);
		}
	};

	const createCondition = () =>
	{
		props.updateCondition(LogicalTree.unpack(
			LogicalTree.createPackedNode('and', getConditionPrototype()))
		);
	};

	const renderEmptyCondition = () =>
	{
		return (
			<div>
				<span>Nenhuma</span>&nbsp;
				<Button
					onClick={() => createCondition()}>
					Criar condição
				</Button>
			</div>
		);
	};

	const renderIntegration = (integ, index) =>
	{
		return (
			<SimpleCollapse 
				key={index} 
				isOpen={false} 
				title={integ.desc}
				style={{marginBottom: '4px'}}>
				<IntegrationForm 
					api={props.api}
					container={props.container}
					store={props.store}
					triggers={props.triggers}
					activities={props.activities}
					refs={props.refs}
					item={integ}
					isMobile
					loadRefs={props.loadRefs}
				/>
			</SimpleCollapse>
		);
	};

	const handleGetFieldOptions = useCallback((item, name) =>
	{
		switch(name)
		{
		case 'collection':
			return Object.entries(props.store.collection.schemaObj.columns)
				.filter(column => !column.auto)
				.map(([key, column]) => ({id: key, ...column}));

		default:
			return [];
		}
	}, []);

	const handleGetFunctionOptions = useCallback(() =>
	{
		return [];
	}, []);

	const {element, onChange} = props;
	if(!element)
	{
		return null;
	}

	return (
		<Container>
			<Row>
				<Col sm="12">
					<b>Properties</b>
				</Col>
			</Row>
			<Field label="Id">
				<Input 
					disabled
					type="text" 
					value={element.id || ''}
					readOnly />
			</Field>
			<Field label="Nome">
				<Input 
					type="text" 
					name="name"
					value={element.name || ''}
					onChange={props.onChange} />
			</Field>

			{element.is === 'SEQ' &&
				<Field label="Conditions">
					<LogicalTree
						tree={element.condition}
						updateTree={props.updateCondition}
						renderPreview={renderConditionPreview}
						renderNode={renderCondition}
						renderEmpty={renderEmptyCondition}
						getNodePrototype={getConditionPrototype}
						onChange={e => onChange({target:{...e.target, name: `condition${e.target.name}`}})}
						className="modal-zindex"
					/>
				</Field>
			}
			
			{element.is === 'SERVICE' &&
				<Field label="Activities">
					<ActivitiesList
						store={props.store}
						isMobile
						element={element}
						activities={props.activities}
						refs={props.refs}
						onChange={onChange}
						loadRefs={props.loadRefs}
						getFieldOptions={(name) => handleGetFieldOptions(element, name)}
						getFunctionOptions={handleGetFunctionOptions}
					/>
				</Field>
			}

			{element.is === 'START' && props.integrations && props.integrations.length > 0 &&
				<Field label="Integrations">
					{
						props.integrations.map((integ, index) => 
							renderIntegration(integ, index))
					}
				</Field>
			}

		</Container>
	);
});

Properties.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object,
	element: PropTypes.object,
	columns: PropTypes.array.isRequired,
	automations: PropTypes.array,
	integrations: PropTypes.array,
	triggers: PropTypes.array.isRequired,
	activities: PropTypes.array.isRequired,
	refs: PropTypes.instanceOf(Map).isRequired,
	loadRefs: PropTypes.func.isRequired,
	onChange: PropTypes.func.isRequired,
	updateCondition: PropTypes.func.isRequired,
};

Properties.defaultProps = {
	automations: {}
};

export default Properties;