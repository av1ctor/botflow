import React, {Component} from 'react';
import PropTypes from 'prop-types';

import './ColumnResizer.css';

export default class ColumnResizer extends Component
{
	static propTypes = 
	{
		getContainer: PropTypes.func.isRequired,
		resizeContainer: PropTypes.bool,
		resizerHelper: PropTypes.func,
		onResize: PropTypes.func.isRequired
	};

	constructor(props)
	{
		super(props);

		this.isResizing = false;

		this.onColumnResizeStart = this.onColumnResizeStart.bind(this);
		this.onColumnResize = this.onColumnResize.bind(this);
	}
	
	getOffset(el) 
	{
		var rect = el.getBoundingClientRect();

		return {
			top: rect.top + document.body.scrollTop,
			left: rect.left + document.body.scrollLeft
		};
	}
		
	onColumnResizeStart(event) 
	{
		let container = this.props.getContainer();
		let containerLeft = this.getOffset(container).left;
		this.column = event.target.parentElement;
		this.isResizing = true;
		this.lastResizerHelperX = (event.pageX - containerLeft + container.scrollLeft);

		this.bindColumnResizeEvents();
	}

	onColumnResize(event) 
	{
		let container = this.props.getContainer();
		container.classList.add('p-unselectable-text');
		let containerLeft = this.getOffset(container).left;
		
		const helper = this.props.resizerHelper();
		helper.style.height = container.offsetHeight + 'px';
		helper.style.top = 0 + 'px';
		helper.style.left = (event.pageX - containerLeft + container.scrollLeft) + 'px';
		helper.style.display = 'block';
	}	
	
	onColumnResizeEnd(event) 
	{
		const helper = this.props.resizerHelper();

		let container = this.props.getContainer();
		let delta = helper.offsetLeft - this.lastResizerHelperX;
		let columnWidth = this.column.offsetWidth;
		let minWidth = this.column.style.minWidth||15;	

		if(columnWidth + delta > parseInt(minWidth, 10)) 
		{
			let width = container.offsetWidth;
			delta = this.props.onResize(delta);
			if(this.props.resizeContainer)
			{
				container.style.width = (width + delta) + 'px';
			}
		}

		helper.style.display = 'none';
		this.column = null;
		container.classList.remove('p-unselectable-text');

		this.unbindColumnResizeEvents();		
	}

	bindColumnResizeEvents() 
	{
		this.documentColumnResizeListener = document.addEventListener('mousemove', (event) => 
		{
			if(this.isResizing) 
			{
				this.onColumnResize(event);
			}
		});
        
		this.documentColumnResizeEndListener = document.addEventListener('mouseup', (event) => 
		{
			if(this.isResizing) 
			{
				this.isResizing = false;
				this.onColumnResizeEnd(event);
			}
		});
	}

	unbindColumnResizeEvents() 
	{
		document.removeEventListener('document', this.documentColumnResizeListener);
		document.removeEventListener('document', this.documentColumnResizeEndListener);
	}
	
	render()
	{
		return (
			<div className="rf-column-resizer" onMouseDown={this.onColumnResizeStart}></div>
		);
	}
}