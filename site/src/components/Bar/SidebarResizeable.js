import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Sidebar} from 'primereact/sidebar';

import './SidebarResizeable.css';

export default class SidebarResizeable 
	extends PureComponent
{
	static propTypes = 
	{
		position: PropTypes.string,
		visible: PropTypes.bool,
		fullScreen: PropTypes.bool,
		onResize: PropTypes.func,
		onHide: PropTypes.func,
		children: PropTypes.any
	};
	
	constructor(props)
	{
		super(props);

		this.isResizing = false;
		this.isReverse = props.position === 'left' || props.position === 'top';
	}
	
	componentDidMount()
	{
		this.bindResizeListeners();
	}

	componentWillUnmount()
	{
		this.unbindResizeListeners();
	}

	handleOnResizeStart = (event) =>
	{
		this.isResizing = true;
		this.sideBar.container.classList.add('p-unselectable-text');
		this.lastPageX = event.pageX;
		this.lastPageY = event.pageY;
	}

	handleOnResizeEnd = (event) =>
	{
		this.isResizing = false;
		this.sideBar.container.classList.remove('p-unselectable-text');
	}

	handleOnResizeX = (event) =>
	{
		if(this.isResizing)
		{
			event.preventDefault();
			let el = this.sideBar.container;

			let deltaX = event.pageX - this.lastPageX;
			let newWidth = el.offsetWidth - (this.isReverse? -deltaX: deltaX);

			if(newWidth > 40 && newWidth < document.documentElement.clientWidth - 40)
			{
				this.sideBar.container.style.width = newWidth + 'px';
			}

			this.lastPageX = event.pageX;

			if(this.props.onResize)
			{
				this.props.onResize(event);
			}
		}
	}

	handleOnResizeY = (event) =>
	{
		if(this.isResizing)
		{
			event.preventDefault();
			let el = this.sideBar.container;

			let deltaY = event.pageY - this.lastPageY;
			let newHeight = el.offsetHeight - (this.isReverse? -deltaY: deltaY);

			if(newHeight > 40 && newHeight < document.documentElement.clientHeight - 40)
			{
				this.sideBar.container.style.height = newHeight + 'px';
			}

			this.lastPageY = event.pageY;

			if(this.props.onResize)
			{
				this.props.onResize(event);
			}
		}
	}

	bindResizeListeners()
	{
		this.documentResizeListener = this.props.position === 'top' || this.props.position === 'bottom'?
			(event) => this.handleOnResizeY(event):
			(event) => this.handleOnResizeX(event);
		this.documentResizeEndListener = (event) => this.handleOnResizeEnd(event);
		
		document.addEventListener('mousemove', this.documentResizeListener);
		document.addEventListener('mouseup', this.documentResizeEndListener);
	}

	unbindResizeListeners()
	{
		if (this.documentResizeListener && this.documentResizeEndListener) 
		{
			document.removeEventListener('mousemove', this.documentResizeListener);
			document.removeEventListener('mouseup', this.documentResizeEndListener);
			this.documentResizeListener = null;
			this.documentResizeEndListener = null;
		}	
	}

	isFullScreen()
	{
		const {position, fullScreen} = this.props;
		return fullScreen !== null && fullScreen !== undefined?
			fullScreen:
			!window.matchMedia(position === 'bottom' || position === 'top'? '(min-height: 768px)': '(min-width: 1024px)').matches;
	}

	renderTopOrBottom(isBottom, children)
	{
		if(isBottom)
		{
			return (
				<>
					<div 
						className="resize-handle-bottom" 
						onMouseDown={this.handleOnResizeStart} />
					{children}
				</>
			);
		}
		else
		{
			return (
				<>
					{children}
					<div 
						className="resize-handle-top" 
						onMouseDown={this.handleOnResizeStart} />
				</>
			);
		}
	}

	renderLeftOrRight(isLeft, children)
	{
		if(isLeft)
		{
			return (
				<>
					{children}
					<div 
						className="resize-handle-left" 
						onMouseDown={this.handleOnResizeStart} />
				</>
			);
		}
		else
		{
			return (
				<>
					<div 
						className="resize-handle-right" 
						onMouseDown={this.handleOnResizeStart} />
					{children}
				</>
			);
		}
	}

	render()
	{
		const {children, ...props} = this.props;
		return (
			<Sidebar 
				ref={el => this.sideBar = el} 
				{...props} 
				fullScreen={this.isFullScreen()}>
				<div 
					className="resize-handle-close"
					onClick={props.onHide}>
					<i className="la la-times-circle" />
				</div>
				{props.position === 'bottom' || props.position === 'top'?
					this.renderTopOrBottom(props.position === 'bottom', children):
					this.renderLeftOrRight(props.position === 'left', children)
				}
			</Sidebar>
		);
	}
}