import React, {useCallback, useEffect, useRef} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Button} from 'reactstrap';
import BaseList from '../../../../../components/BaseList';
import BaseForm from '../../../../../components/BaseForm';
import Misc from '../../../../../libs/Misc';
import Markdown from '../../../../../libs/Markdown';
import MarkdownEditor from '../../../../../components/Editor/MarkdownEditor';
import { observer } from 'mobx-react-lite';

const Posts = observer(props =>
{
	const store = props.store;

	const baseList = useRef();
	
	useEffect(() =>
	{
		baseList.current && baseList.current.onResize(props.resizeEvent);
	}, [props.resizeEvent]);

	const getHeader = () =>
	{
		return [
			{name: null, title: <div className="rf-col-topic-title">{props.topic.title}</div>, size: {xs:12}, klass: ''},
		];
	};

	const renderFields = (post, onChange) =>
	{
		return (
			<>
				{post.id &&
					<Row>
						<Col sm="12">
							<Label className="base-form-label">Id</Label>
							<Input 
								type="text" 
								value={post.id} 
								readOnly
								disabled />
						</Col>
					</Row>
				}
				{post.type === 'TOPIC' &&
					<Row>
						<Col sm="12">
							<Label className="base-form-label">Título</Label>
							<Input 
								name="title"						
								type="text" 
								value={post.title || ''} 
								required
								onChange={onChange} />
						</Col>
					</Row>
				}
				<Row>
					<Col sm="12">
						<Label className="base-form-label">Mensagem</Label>
						<MarkdownEditor 
							value={post.message || ''}
							options={{minHeight: '100px'}} 
							onChange={(value) => onChange({target: {name: 'message', value: value}})} 
						/>
					</Col>
				</Row>
				{post.id &&
					<Row>
						<Col sm="4">
							<Label className="base-form-label">Usuário</Label>
							<Input 
								type="text" 
								value={post.createdBy? post.createdBy.email: ''} 
								readOnly />
						</Col>					
						<Col sm="4">
							<Label className="base-form-label">Created at</Label>
							<Input 
								type="text" 
								value={Misc.dateToString(post.createdAt) || ''} 
								readOnly />
						</Col>
						<Col sm="4">
							<Label className="base-form-label">Updated at</Label><br/>
							<Input 
								type="text" 
								value={Misc.dateToString(post.updatedAt) || ''} 
								readOnly />
						</Col>
					</Row>
				}	
			</>
		);
	};

	const handleSubmitted = useCallback((post, creating) =>
	{
		if(post.type === 'TOPIC')
		{
			props.onReload();
		}

		if(creating)
		{
			baseList.current.closeCreateForm();
		}
	}, []);

	const renderPost = (post, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<Col xs="12">
					<Row> 
						<Col 
							xs="12"
							className="rf-col-post-message"
							dangerouslySetInnerHTML={{__html: Markdown.render(post.message)}}>
						</Col>
					</Row>
					<Row>
						<Col xs="12" className="rf-col-post-user">
							{post.createdBy && 
								<>
									<div className="rf-user-avatar-mini">
										<i className={'ava-' + post.createdBy.icon}/>
									</div>
									<div className="rf-user-nick">
										{post.createdBy.nick} @ {Misc.dateToString(post.createdAt)}
									</div>
								</>
							}
						</Col>
					</Row>
				</Col>
			);
		}

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<BaseForm
							key={post.id || 'empty'}
							api={props.api}
							history={null}
							parent={baseList.current}
							controller={`collections/${store.collection.id}/items/${props.item._id}/posts`}
							item={post}
							onLoad={transformForEditing}
							render={renderFields}
							onSubmit={transformForSubmitting}
							onSubmitted={(item) => handleSubmitted(item, !post.id)}
						/>
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

	const getEmpty = (topic) =>
	{
		return {
			id: null,
			parentId: topic.id,
			message: '',
		};
	};

	const handleReply = useCallback((item) =>
	{
		baseList.current.showCreateForm({
			id: null,
			parentId: item.id,
			message: `**@${item.createdBy.email}** disse:\n` +
			`${'> ' + item.message.split('\n').join('\n> ')}\n\n`
		});
	}, []);

	const renderActions = (item) =>
	{
		return (
			<>
				<Button 
					size="xs" 
					outline 
					className="base-list-button" 
					onClick={() => handleReply(item)} 
					title="Responder">
					<i className="base-list-button-add-icon la la-at"></i>
				</Button>
			</>
		);
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
		<div className="rf-col-post-replies-pane">
			<BaseList
				ref={baseList}
				key={props.topic.id}
				inline
				controller={`collections/${collection.id}/items/${props.item._id}/posts/${props.topic.id}`}
				updateHistory={false}
				sortBy='createdAt'
				ascending={true}
				addButtonText="Responder"
				height={props.height}
				api={props.api}
				container={props.container} 
				itemTemplate={getEmpty(props.topic)}
				getHeader={getHeader}
				onLoad={handleItemsLoaded}
				renderItem={renderPost}
				renderActions={renderActions}
			/>
		</div>
	);
});

Posts.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	item: PropTypes.object,
	topic: PropTypes.object,
	visible: PropTypes.bool.isRequired,
	height: PropTypes.number,
	resizeEvent: PropTypes.any,
	onReload: PropTypes.func.isRequired,
};

export default Posts;