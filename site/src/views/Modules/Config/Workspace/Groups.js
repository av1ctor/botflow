import React, {useEffect, useRef, useState} from 'react';
import PropTypes from 'prop-types';
import OrgChartDraggable from '../../../../components/OrgChart/OrgChartDraggable';
import {Button} from 'reactstrap';

import './Groups.css';

const Groups = ({api, container}) =>
{
	const [groups, setGroups] = useState([]);
	const [saving, setSaving] = useState(false);

	const instance = useRef({
		idCnt: 0
	});

	useEffect(() =>
	{
		loadGroups();
	}, []);

	const loadGroups = async () =>
	{
		const res = await api.get('groups?sort=id,asc');
		if(res.errors)
		{
			container.showMessage(res.errors, 'error');
			return;
		}

		setGroups(transformGroupsForEditing(res.data));
	};

	const transformGroupsForEditing = (groups) =>
	{
		const toNode = (group, parent) => 
		{			
			const node = {
				id: group.id,
				parent: parent,
				label: group.name,
				dirty: false,
			};

			node.children = group.children?
				group.children.map(id => toNode(groups.find(g => g.id === id), node)):
				null;

			return node;
		};

		return groups
			.filter(group => !group.parentId)
			.map(group => toNode(group, null));
	};

	const findById = (id, groups) =>
	{
		for(let i = 0; i < groups.length; i++)
		{
			const group = groups[i];
			if(group.id === id)
			{
				return group;
			}

			if(group.children)
			{
				const res = findById(id, group.children);
				if(res !== null)
				{
					return res;
				}
			}
		}

		return null;
	};

	const genId = () =>
	{
		return ++instance.current.idCnt;
	};

	const handleAdd = (node) =>
	{
		const groups_ = Array.from(groups);

		node.children = Array.from(node.children || []);

		node.children.push(
			{
				id: genId(),
				parent: node,
				label: '',
				dirty: true,
				editing: true,
				children: null
			}
		);

		setGroups(groups_);
	};

	const isNew = (node) =>
	{
		return typeof node.id !== 'string';
	};

	const handleDel = (node) =>
	{
		const groups_ = Array.from(groups);

		if(isNew(node))
		{
			const parent = node.parent;
			parent.children = parent.children.filter(group => group.id !== node.id);
		}
		else
		{
			node.deleted = true;
			node.dirty = true;
		}
		
		setGroups(groups_);
	};
	
	const handleChange = (event, node) =>
	{
		const value = event.target.value;
		
		const groups_ = Array.from(groups);

		node.dirty = node.label !== value;
		node.label = value;

		setGroups(groups_);
	};

	const handleEdit = (node) =>
	{
		const groups_ = Array.from(groups);

		node.editing = !node.editing;

		setGroups(groups_);
	};

	const handleMove = (from, to) =>
	{
		const groups_ = Array.from(groups);

		if(from.parent)
		{
			from.parent.children = from.parent.children
				.filter(child => child.id !== from.id);
		}

		const dest = findById(to.id, groups_);

		if(!dest.children)
		{
			dest.children = [from];
		}
		else
		{
			dest.children.push(from);
		}

		from.dirty = from.parent.id !== dest.id;
		from.parent = dest;

		setGroups(groups_);
	};

	const validate = (changed) =>
	{
		const errors = [];
		for(let i = 0; i < changed.length; i++)
		{
			const node = changed[i];
			
			const label = node.label;
			const parent = node.parent; 
			if(!label || label.trim().length === 0)
			{
				errors.push(`Nome não pode estar em branco abaixo de '${parent && parent.label}'`);
			}

			if(parent && 
				parent.children.find(child => child.label === label && child.id !== node.id))
			{
				errors.push(`Nome '${label}' duplicado abaixo de: '${parent.label}'`);
			}
		}

		if(errors.length > 0)
		{
			container.showMessage(errors, 'error');
			return false;
		}

		return true;
	};

	const handleSave = async () =>
	{
		const getChanged = (nodes) => 
			nodes.filter(node => node.dirty)
				.concat(...nodes.map(node => node.children && node.children.length > 0? getChanged(node.children): null))
				.filter(elm => !!elm);

		const transform = (node, isNewNode) => ({
			id: isNewNode? 
				undefined: 
				node.id,
			name: node.label,
			parentId: node.parent?
				isNew(node.parent)?
					null:
					node.parent.id:
				null
		});
		
		const changed = getChanged(groups);

		if(!validate(changed))
		{
			return;
		}

		try
		{
			setSaving(true);

			const ops = [];

			for(let i = 0; i < changed.length; i++)
			{
				const node = changed[i];

				if(isNew(node))
				{
					ops.push({
						op: 'CREATE',
						group: transform(node, true)
					});
				}
				else
				{
					if(node.deleted)
					{
						ops.push({
							op: 'DELETE',
							id: node.id
						});
					}
					else
					{
						ops.push({
							op: 'UPDATE',
							id: node.id,
							group: transform(node, false)
						});
					}
				}
			}

			if(ops.length > 0)
			{
				const res = await api.patch('groups', ops);
				if(res.errors)
				{
					container.showMessage(res.errors, 'error');
				}
				else
				{
					container.showMessage('Grupos atualizados!', 'success');
					loadGroups();
				}
			}
		}
		finally
		{
			setSaving(false);
		}
	};

	const renderNode = (node) =>
	{
		return (
			<>
				<div className="rf-group-header">
					<div 
						className="rf-group-btn"
						title="Adicionar"
						onClick={() => handleAdd(node)}>
						<i className="la la-plus-square" />
					</div>
					<div 
						className={`rf-group-btn ${!node.parent? 'disabled': ''}`}
						title="Remover"
						onClick={node.parent? () => handleDel(node): null}>
						<i className="la la-trash" />
					</div>
					<div 
						className="rf-group-btn"
						title="Mover">
						<i className="la la-bars" />
					</div>
				</div>
				<div 
					className="rf-group-name"
					title="Editar">
					{node.editing?
						<form onSubmit={() => handleEdit(node)}>
							<input
								type="text"
								className="rf-group-input"
								value={node.label}
								autoFocus
								onChange={(e) => handleChange(e, node)} 
								onBlur={() => handleEdit(node)} />
						</form>:
						<div 
							className="rf-group-name-value" 
							onClick={() => handleEdit(node)}>{node.label||<span>&nbsp;</span>}
						</div>
					}
				</div>
			</>
		);
	};

	if(!groups || groups.length === 0)
	{
		return null;
	}
	
	return (
		<div className="rf-page-contents rf-group-container">
			<div className="rf-page-header pt-2">
				<div>
					<div className="rf-page-title-icon">
						<i className="la la-sitemap" />
					</div>
					<div className="rf-page-title-other">
						<div className="row">
							<div className="col-auto rf-page-title-container">
								<div className="rf-page-title">
									Configurações / Área de trabalho / Grupos
								</div>
							</div>
						</div>
						<div className="row">
							<div className="col">
								<small>Organograma de grupos de usuários da área de trabalho</small>
							</div>
						</div>
					</div>
				</div>
			</div>

			<OrgChartDraggable
				value={groups}
				className="rf-group-node"
				onMove={handleMove}
				nodeTemplate={renderNode}
			/>
			<div className="rf-group-buttons">
				<Button
					onClick={loadGroups}>
					Recarregar
				</Button>
				<Button
					disabled={saving}
					color="danger"
					onClick={handleSave}>
					Atualizar
				</Button>
			</div>
		</div>
	);
};

Groups.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired
};

export default Groups;