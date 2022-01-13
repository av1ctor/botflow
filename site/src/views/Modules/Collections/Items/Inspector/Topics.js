import React, {useCallback, useEffect, useRef} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Badge} from 'reactstrap';
import BaseList from '../../../../../components/BaseList';
import BaseForm from '../../../../../components/BaseForm';
import MarkdownEditor from '../../../../../components/Editor/MarkdownEditor';
import Misc from '../../../../../libs/Misc';
import Posts from './Posts';

const Topics = observer(props =>
{
	const store = props.store;

	const baseList = useRef();

	useEffect(() =>
	{
		baseList.current && baseList.current.onResize(props.resizeEvent);
	}, [props.resizeEvent]);
	
	const handleReload = () =>
	{
		baseList.current.reload();
	};

	const getHeader = () =>
	{
		return [
			{name: null, title: 'Título', size: {xs:12, md:6}, klass: ''},
			{name: null, title: 'Usuário', size: {md:3}, klass: 'd-none d-md-block'},
			{name: 'createdAt', title: 'Data', size: {md:3}, klass: 'd-none d-md-block'},
		];
	};

	const renderFields = (topic, onChange) =>
	{
		return (
			<>
				<Row>
					<Col sm="12">
						<Label className="base-form-label">Título</Label>
						<Input 
							name="title"						
							type="text" 
							value={topic.title || ''} 
							required
							onChange={onChange} />
					</Col>
				</Row>
				<Row>
					<Col sm="12">
						<Label className="base-form-label">Mensagem</Label>
						<MarkdownEditor 
							value={topic.message || ''} 
							options={{minHeight: '100px'}} 
							onChange={(value) => onChange({target: {name: 'message', value: value}})} 
						/>
					</Col>
				</Row>
				{topic.id && 
					<Row>
						<Col sm="4">
							<Label className="base-form-label">Usuário</Label>
							<Input 
								type="text" 
								value={topic.createdBy? topic.createdBy.email: ''} 
								readOnly />
						</Col>					
						<Col sm="4">
							<Label className="base-form-label">Created at</Label>
							<Input 
								type="text" 
								value={Misc.dateToString(topic.createdAt) || ''} 
								readOnly />
						</Col>
						<Col sm="4">
							<Label className="base-form-label">Updated at</Label><br/>
							<Input 
								type="text" 
								value={Misc.dateToString(topic.updatedAt) || ''} 
								readOnly />
						</Col>
					</Row>
				}
			</>
		);
	};

	const handleOnSubmitted = (topic, creating) =>
	{
		if(creating)
		{
			baseList.current.closeCreateForm();
		}
	};

	const renderTopic = (topic, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="12" md="6">
						<div className="rf-col-post-title-icon">{topic.title.charAt(0)}</div>
						<div className="rf-col-post-title">{topic.title}</div>
						<Badge pill>{topic.posts}</Badge>
					</Col>
					<Col md="3" className="d-none d-md-block">
						{topic.createdBy && <><div className="rf-user-avatar-mini"><i className={'ava-' + topic.createdBy.icon}/></div><div className="rf-user-nick">&nbsp;{topic.createdBy.nick}</div></>}
					</Col>
					<Col md="3" className="d-none d-md-block">
						{Misc.dateToString(topic.createdAt)}
					</Col>
				</>
			);
		}

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						{!topic.id? 
							<BaseForm
								key={topic.id || 'empty'}
								api={props.api}
								history={null}
								parent={baseList.current}
								controller={`collections/${store.collection.id}/items/${props.item._id}/posts`}
								item={topic}
								onLoad={transformForEditing}
								render={renderFields}
								onSubmit={transformForSubmitting}
								onSubmitted={(item) => handleOnSubmitted(item, !topic.id)}
							/>:
							<Posts
								{...props}
								topic={topic}
								height={baseList.current.getDimensions().height}
								onReload={handleReload}
							/>
						}
					</Col>
				</Row>
			</Col>
		);		
	};

	const transformForEditing = (item) =>
	{
		return {
			...item,
			createdAt: Misc.stringToDate(item.createdAt),
			updatedAt: Misc.stringToDate(item.updatedAt),
		};
	};

	const transformForSubmitting = (item) =>
	{
		return {
			...item,
			createdBy: undefined,
			createdAt: undefined,
			updatedBy: undefined,
			updatedAt: undefined,
		};
	};

	const getEmpty = () =>
	{
		return {
			parentId: null,
			id: null,
			title: '',
			message: '',
		};
	};

	const handleItemsLoaded = useCallback((items) =>
	{
		return items.map(item => transformForEditing(item));
	}, []);

	if(!props.visible)
	{
		return null;
	}

	const collection = store.collection;
	if(!collection)
	{
		return null;
	}

	return (
		<BaseList
			ref={baseList}
			inline
			controller={`collections/${collection.id}/items/${props.item._id}/posts`}
			updateHistory={false}
			sortBy='createdAt'
			ascending={false}
			api={props.api}
			container={props.container}
			mode='SLIDED'
			addButtonText="Comentar" 
			itemTemplate={getEmpty()}
			getHeader={getHeader}
			onLoad={handleItemsLoaded}
			renderItem={renderTopic}
		/>
	);
});

Topics.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	item: PropTypes.object.isRequired,
	resizeEvent: PropTypes.any,
};

export default Topics;