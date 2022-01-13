import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import {sortableContainer, sortableElement, sortableHandle} from 'react-sortable-hoc';
import {Badge, Button} from 'reactstrap';
import classNames from 'classnames';
import CollapseUnmounted from '../Collapse/CollapseUnmounted';
import Misc from '../../libs/Misc';

import './LogicalTree.css';

//FIXME: when a node is added or moved, we must reorder the tree or sortable-hoc will fail

const DragHandle = sortableHandle(({index}) => 
	<div 
		className="rf-logtree-handle"
		title="Mover condição">
		<Badge className="rf-logtree-handle-badge">{index !== null? index+1: <i className="la la-bars" />}</Badge>
	</div>);

const SortableContainer = sortableContainer(({children}) => 
{
	return (
		<div 
			className="rf-logtree-container">
			{children}
		</div>);
});

export default class LogicalTree extends PureComponent
{
	static propTypes = {
		tree: PropTypes.object,
		getNodePrototype: PropTypes.func,
		updateTree: PropTypes.func,
		renderPreview: PropTypes.func.isRequired,
		renderNode: PropTypes.func.isRequired,
		renderEmpty: PropTypes.func.isRequired,
		onChange: PropTypes.func.isRequired,
		className: PropTypes.string,
		disabled: PropTypes.bool,
		allowNot: PropTypes.bool,
	};

	static defaultProps = {
		allowNot: true
	};

	constructor(props)
	{
		super(props);

		this.state = {
			expanded: new Map()
		};
	}

	static createPackedNode(op = 'and', cond)
	{
		return {
			[op]: [
				{
					cond: cond
				}
			]
		};
	}

	static unpack(
		node, 
		transformNode = (node) => node, 
		tree = {},
		idRef = {id: 0}, 
		parentId = null)
	{
		if(!node)
		{
			return null;
		}
		
		const nodeId = idRef.id;

		let op = 'and';
		let children = [];
		if(node)
		{
			op = node.and !== undefined? 
				'and':
				node.or !== undefined? 
					'or': 
					null;

			children = op? 
				node.and || node.or || []:
				null;
		}

		tree[nodeId] = op? 
			{
				op: op,
				neg: node? node.neg: false,
				_meta: {
					id: nodeId,
					parentId: parentId,
					childrenIds: children.map(child => {
						const childId = ++idRef.id;
						this.unpack(child, transformNode, tree, idRef, nodeId);
						return childId;
					}),
					lastId: idRef.id,
				},
			}:
			{
				...transformNode(node.cond || {}),
				_meta: {
					id: nodeId,
					parentId: parentId,
				}
			};

		return tree;
	}

	static pack(
		tree, 
		transformNode = (node) => node, 
		id = 0)
	{
		const getAllButMeta = (node) => ({...node, _meta: undefined});
		
		if(!tree)
		{
			return undefined;
		}

		const node = tree[id];
		const meta = node._meta;

		if(meta.childrenIds)
		{
			if(meta.childrenIds.length === 0)
			{
				return undefined;
			}

			const children = meta.childrenIds
				.map(childId => this.pack(tree, transformNode, childId))
				.filter(v => v !== undefined);

			return children.length > 0?
				{
					neg: node.neg? 
						true: 
						undefined,
					[node.op]: children,
				}:
				undefined;
		}
		else
		{
			const cond = transformNode(getAllButMeta(node));
			return cond?
				{cond: cond}:
				undefined;
		}
	}

	addToParent(tree, id, parentId)
	{
		tree[parentId]._meta.childrenIds = 
			tree[parentId]._meta.childrenIds.concat(id);
	}

	removeFromParent(tree, id, parentId)
	{
		if(parentId === null)
		{
			return;
		}

		tree[parentId]._meta.childrenIds = 
			tree[parentId]._meta.childrenIds.filter(i => i !== id);
	}

	updateChildrensParent(tree, node, toId)
	{
		node._meta.childrenIds.forEach(id => {
			const child = tree[id];
			child._meta.parentId = toId;
		});
	}

	updateChildrenToParent(node, fromId, toId)
	{
		node._meta.childrenIds = 
			node._meta.childrenIds.map(id => id === fromId? toId: id);
	}
	
	addNode(node, parentId)
	{
		const tree = Object.assign({}, this.props.tree);

		const id = ++tree[0]._meta.lastId;

		const meta = node._meta;
		meta.parentId = parentId;
		
		if(parentId !== null)
		{
			meta.id = id;
			tree[id] = node;
			this.addToParent(tree, id, parentId);
		}
		else
		{
			const root = tree[0];
			root._meta.id = id;
			this.updateChildrensParent(tree, root, id);

			meta.id = 0;
			meta.lastId = root._meta.lastId;
			this.updateChildrensParent(tree, node, 0);
			this.updateChildrenToParent(node, 0, id);
			
			tree[id] = root;
			tree[0] = node;
		}

		this.handleToggleNode(null, meta.id);

		return tree;
	}

	handleAddLogical = (parentId) =>
	{
		const node = {
			op: 'and',
			_meta: {
				childrenIds: []
			}
		};

		this.props.updateTree(this.addNode(node, parentId));
	}

	handleAddLeaf = (parentId) =>
	{
		const node = {
			...this.props.getNodePrototype(),
			_meta: {
			}
		};

		this.props.updateTree(this.addNode(node, parentId));
	}

	handleDeleteNode = (event, id) =>
	{
		event && event.stopPropagation();

		let tree = Object.assign({}, this.props.tree);

		const parentId = tree[id]._meta.parentId;
		this.removeFromParent(tree, id, parentId);

		tree[id] = undefined;

		if(parentId === 0)
		{
			if(tree[0]._meta.childrenIds.length === 0)
			{
				tree = null;
			}
		}

		this.props.updateTree(tree);
	}

	isExpanded(id)
	{
		const {expanded} = this.state;
		return expanded.has(id) && expanded.get(id);
	}

	handleToggleNode = (event, id) =>
	{
		event && event.stopPropagation();
		
		const expanded = new Map(this.state.expanded);

		expanded.set(id, !this.isExpanded(id));

		this.setState({
			expanded: expanded
		});
	}

	handleChangeLogOp = (id, op) => 
	{
		const node = this.props.tree[id];
		if(op === 'not')
		{
			this.props.onChange({
				target: {
					name: `[${id}].neg`,
					value: !node.neg
				}
			});
		}
		else
		{
			this.props.onChange({
				target: {
					name: `[${id}].op`,
					value: op
				}
			});
		}
	}

	handleDrop = ({oldIndex, newIndex}) =>
	{
		if(newIndex === 0)
		{
			return;
		}
		
		const tree = Object.assign({}, this.props.tree);
		
		const from = tree[oldIndex];
		const to = tree[newIndex];

		if(!from || !to)
		{
			return;
		}

		const parentFrom = tree[from._meta.parentId];
		const parentTo = tree[to._meta.parentId];

		if(parentFrom === parentTo)
		{
			const children = parentFrom._meta.childrenIds;
			const fromIndex = children.indexOf(oldIndex);
			const toIndex = children.indexOf(newIndex);
			parentFrom._meta.childrenIds = 
				Misc.moveArray(children, fromIndex, toIndex);
		}
		else
		{
			const children = parentTo._meta.childrenIds;
			const toIndex = children.indexOf(newIndex);
			parentTo._meta.childrenIds = 
				Misc.addToArray(children, toIndex, [oldIndex]);

			from._meta.parentId = parentTo._meta.id;

			parentFrom._meta.childrenIds = 
				parentFrom._meta.childrenIds.filter(i => i !== oldIndex);
		}

		this.props.updateTree(tree);
	}

	renderLogOp(node)
	{
		const renderOp = (oper, op) =>
			<div 
				className={classNames('rf-logtree-op-log-button', oper, {
					'active': oper === op,
					'disabled': disabled})}
				onClick={disabled? null: () => this.handleChangeLogOp(id, oper)}>
				{oper.toUpperCase()}
			</div>;

		const meta = node._meta;
		const op = node.op;
		const id = meta.id;	
		
		const {disabled, allowNot} = this.props;
		const canDelete = id !== 0;

		return (
			<div className="rf-logtree-op-header">
				<div className="rf-logtree-op-header-opers">
					{id !== 0 && <DragHandle index={null} />}
					{allowNot && renderOp('not', node.neg? 'not': '')}
					{renderOp('and', op)}
					{renderOp('or', op)}
				</div>
				<div className="rf-logtree-op-header-actions">
					<div 
						className={classNames('rf-logtree-op-header-actions-button', {
							'disabled': disabled})}
						onClick={disabled? null: () => this.handleAddLogical(meta.id)}
						title="Adicionar operador lógico">
						<i className="la la-brain" /><i className="rf-logtree-op-plus la la-plus-circle" />
					</div>
					{
						canDelete &&
						<div 
							className="rf-logtree-op-header-actions-button"
							onClick={disabled? null: (e) => this.handleDeleteNode(e, id)}
							title="Remover operador lógico">
							<i className="la la-trash" />
						</div>
					}
				</div>
			</div>
		);
	}

	renderLeaf(node, index, length, asTree)
	{
		const meta = node._meta;
		const id = meta.id;
		const isExpanded = this.isExpanded(id);
		const {disabled} = this.props;

		return (
			<div 
				key={id}
				className={asTree? classNames('rf-logtree-val', {
					'first': index === 0, 
					'last': index === length-1}):
					'rf-logtree-val-single'}>
				<div 
					className="rf-logtree-val-preview"
					onClick={event => this.handleToggleNode(event, id)}>
					{asTree? 
						<DragHandle index={index} />:
						<Badge className="rf-logtree-handle-badge">{index+1}</Badge>
					}
					{this.props.renderPreview(node) || <span>&nbsp;</span>}
					<div className="rf-logtree-val-actions">
						{!disabled && 
							<div 
								className="rf-logtree-op-header-actions-button"
								onClick={(e) => this.handleDeleteNode(e, id)}
								title="Remover condição">
								<i className="la la-trash" />
							</div>
						}
						<div 
							className="rf-logtree-op-header-actions-button"
							onClick={event => this.handleToggleNode(event, id)}
							title={`${isExpanded? 'Fechar': 'Expandir'}`}>
							<i className={`la la-caret-${isExpanded? 'up': 'down'}`} />
						</div>
					</div>
				</div>
				<CollapseUnmounted 
					isOpen={isExpanded} 
					className="rf-logtree-val-item">
					{this.props.renderNode(node, id)}
				</CollapseUnmounted>
			</div>
		);
	}

	TreeNode = sortableElement(({node, index_, length}) =>
	{
		const meta = node._meta;
		const children = meta.childrenIds;
		if(!children)
		{
			return this.renderLeaf(node, index_, length, length > 1 || node.neg);
		}
		else
		{
			const {disabled} = this.props;
			const asTree = (children && children.length > 1) || node.neg;

			const TreeNode = this.TreeNode;
			return (
				<div 
					key={meta.id}
					className={classNames('rf-logtree-cond', {
						'top': meta.id === 0, 
						'first': index_ === 0, 
						'last': index_ === length-1,
						'disabled': this.props.disabled})}>
					{asTree &&
						this.renderLogOp(node, asTree)
					}
					{children.map((id, index) => 
						<TreeNode 
							key={id}
							index={id}
							index_={index}
							node={this.props.tree[id]}
							length={children.length} />
					)}
					{!disabled &&
						<div className="rf-logtree-buttons">
							<Button 
								type="button"
								size="xs" 
								outline 
								className="base-list-button" 
								color="primary"
								title="Adicionar" 
								onClick={() => this.handleAddLeaf(meta.id)}>
								<i className="la la-plus-circle" /> 
								<span className="base-list-button-add">{' '}Adicionar condição</span>
							</Button>
						</div>
					}
				</div>
			);		
		}
	});

	render()
	{
		const TreeNode = this.TreeNode;

		return (
			<SortableContainer 
				onSortEnd={this.handleDrop} 
				useDragHandle 
				helperClass={this.props.className}>
				{!this.props.tree?
					this.props.renderEmpty():
					<TreeNode
						key={'root'}
						node={this.props.tree[0]}
						index={0}
						index_={0}
						length={1}
						disabled />
				}
			</SortableContainer>
		);
	}
}