import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import {useHistory} from 'react-router-dom';

const Submenu = (props) =>
{
	if(!props.items)
	{
		return null;
	}
	
	const renderItem = (item) =>  
	{
		const styleClass = classNames(item.badgeStyleClass, {'active-menuitem': item.active}, 'rf-menu-item');
		const badge = item.badge && <span className="menuitem-badge">{item.badge}</span>;
		const submenuIcon = item.items && <i className="la la-angle-down menuitem-toggle-icon"></i>;
		const contextMenu = item.contextMenu?
			<div 
				className="rf-menu-handle" 
				onClick={(event) => item.contextMenu(event, item)}
			>
				<i className="la la-caret-down" />
			</div>: null;

		return (
			<li 
				className={styleClass} 
				key={item.index}
			>
				{item.items && props.root===true && <div className='arrow'></div>}
				<a 
					href={item.url} 
					onClick={(e) => props.onMenuItemClick(e, item, item.index)} 
					target={item.target}
				>
					{item.icon && <i className={item.icon}></i>}
					<span>{item.label}</span>
					{submenuIcon}
					{badge}
				</a>
				{contextMenu}
				{item.items && 
				<Submenu 
					items={item.items} 
					onMenuItemClick={props.onMenuItemClick} 
					history={props.history} />}
			</li>
		);
	};
		
	return (
		<ul className={props.className}>
			{props.items.map((item) => renderItem(item))}
		</ul>
	);
};

Submenu.defaultProps = 
{
	className: null,
	items: [],
	onMenuItemClick: null,
	root: false
};

Submenu.propTypes = 
{
	className: PropTypes.string,
	items: PropTypes.array,
	onMenuItemClick: PropTypes.func,
	root: PropTypes.bool,
	history: PropTypes.object
};

const Menu = (props) =>
{
	const history = useHistory();
	const [items, setItems] = useState(props.items);

	useEffect(() =>
	{
		if(props.items)
		{
			var currentPath = history.location.pathname;
			if(currentPath !== '/')
			{
				props.items.forEach((item) =>
				{
					if(item.path === currentPath)
					{
						//setState({activeIndex: item.index});
					}
				});
			}

		}
	}, []);
	
	useEffect(() =>
	{
		setItems(props.items);
	}, [props.items]);

	const findItem = (items, index) =>
	{
		const item = items.find(i => i.index === index);
		if(item)
		{
			return item;
		}

		for(let i = 0; i < items.length; i++)
		{
			const item = items[i];
			if(item.items)
			{
				const res = findItem(item.items, index);
				if(res)
				{
					return res;
				}
			}
		}

		return null;
	};

	const toggleOff = (items) =>
	{
		for(let i = 0; i < items.length; i++)
		{
			const item = items[i];
			item.active = false;

			if(item.items)
			{
				toggleOff(item.items);
			}
		}
	};

	const toggleTree = (items, item) =>
	{
		while(item)
		{
			if(!item.parentIndex)
			{
				break;
			}
			
			const parent = findItem(items, item.parentIndex);
			parent.active = true;
			item = parent;
		}
	};

	const onMenuItemClick = (event, item, index) =>
	{
		if(item.disabled) 
		{
			event.preventDefault();
			return true;
		}
											
		if(item.command) 
		{
			item.command({originalEvent: event, item: item});
		}

		if(item.items || !item.url) 
		{
			event.preventDefault();
		}

		if(!item.items && props.onMenuItemClick) 
		{
			props.onMenuItemClick({
				originalEvent: event,
				item: item
			});
		}

		setItems(items =>
		{
			const items_ = Array.from(items);
			item = findItem(items_, index);
			const active = item.items? !item.active: true;
			toggleOff(items_);
			item.active = active;
			toggleTree(items_, item);
	
			return items_;				
		});
	};

	return (
		<div className="menu">
			<Submenu 
				items={items} 
				className="layout-main-menu" 
				onMenuItemClick={onMenuItemClick} 
				root={true} 
				history={history} />
		</div>);
};

Menu.defaultProps = {
	items: [],
	onMenuItemClick: null
};

Menu.propTypes = {
	items: PropTypes.array,
	onMenuItemClick: PropTypes.func,
};

export default Menu;