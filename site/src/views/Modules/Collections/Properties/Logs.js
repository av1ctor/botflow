import React, {useCallback, useRef} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Badge} from 'reactstrap';
import BaseList from '../../../../components/BaseList';
import BaseForm from '../../../../components/BaseForm';
import Misc from '../../../../libs/Misc';

const Logs = observer(props =>
{
	const store = props.store;
	
	const baseList = useRef();
	
	const getHeader = () =>
	{
		return [
			{name: null, title: 'Tipo', size: {xs:4, md:2}, klass: ''},
			{name: null, title: 'Usuário', size: {md:3}, klass: 'd-none d-md-block'},
			{name: null, title: 'Mensagem', size: {xs:8, md:4}, klass: ''},
			{name: 'date', title: 'Data', size: {md:3}, klass: 'd-none d-md-block'},
		];
	};

	const renderFields = (item) =>
	{
		return (
			<>
				<Row>
					<Col sm="4">
						<Label className="base-form-label">Tipo</Label><br/>
						<Input 
							type="text" 
							value={item.type} 
							readOnly />
					</Col>
					<Col sm="4">
						<Label className="base-form-label">Usuário</Label>
						<Input 
							type="text" 
							value={item.user? item.user.email: ''} 
							readOnly />
					</Col>					
					<Col sm="4">
						<Label className="base-form-label">Data</Label>
						<Input 
							type="text" 
							value={Misc.dateToString(item.date)} 
							readOnly />
					</Col>
				</Row>
				<Row>
					<Col sm="12">
						<Label className="base-form-label">Mensagem</Label>
						<Input 
							type="textarea" 
							rows="5"
							value={item.message} 
							readOnly />
					</Col>
				</Row>
				<Row>
					<Col sm="12">
						<Label className="base-form-label">Extra</Label>
						<Input 
							type="textarea" 
							rows="5"
							value={item.extra? JSON.stringify(JSON.parse(item.extra), null, 2): ''} 
							readOnly />
					</Col>
				</Row>
			</>
		);
	};

	const renderItem = (item, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="4" md="2">
						<Badge
							color={item.type === 'ERROR'? 'danger': 'primary'}>
							{item.type}
						</Badge>
					</Col>
					<Col md="3" className="d-none d-md-block">
						{item.user? item.user.email: ''}
					</Col>
					<Col xs="8" md="4">
						{item.message}
					</Col>
					<Col md="3" className="d-none d-md-block">
						{Misc.dateToString(item.date)}
					</Col>
				</>
			);
		}

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<BaseForm
							key={item.id} 
							api={props.api}
							history={null}
							parent={baseList.current}
							item={item}
							enabled={false}
							render={renderFields} 
						/>
					</Col>
				</Row>
			</Col>
		);		
	};

	const transformItem = (item) =>
	{
		return {
			...item,
			date: Misc.stringToDate(item.date)
		};
	};

	const handleItemsLoaded = useCallback((items) =>
	{
		return items.map(item => transformItem(item));
	}, []);

	const collection = store.collection;

	if(!collection || !props.visible)
	{
		return null;
	}

	return (
		<Row>
			<Col sm="12" style={{minHeight: props.height}}>
				<BaseList
					ref={baseList}
					inline
					height={props.height}
					controller={props.path || `collections/${collection.id}/logs`}
					updateHistory={false}
					sortBy='date'
					ascending={false}
					api={props.api}
					container={props.container} 
					getHeader={getHeader}
					onLoad={handleItemsLoaded}
					renderItem={renderItem}
				/>
			</Col>
		</Row>
	);
});

Logs.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
	path: PropTypes.string,
	height: PropTypes.number,
};

Logs.defaultProps = {
	height: 700
};

export default Logs;