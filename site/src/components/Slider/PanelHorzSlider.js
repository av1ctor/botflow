import React from 'react';
import PropTypes from 'prop-types';
import {Swipeable} from 'react-swipeable';

import './PanelHorzSlider.css';

//based on: https://github.com/kapost/react-component-slider
export default class PanelHorzSlider extends React.Component 
{
	static propTypes = {
		children: PropTypes.any.isRequired,
		className: PropTypes.string
	};
	
	constructor(props) 
	{
		super(props);

		this.toggleArrows = this.toggleArrows.bind(this);
	}

	componentDidMount() 
	{
		window.addEventListener('resize', this.handleResize);
		this.resetMargin();
	}

	componentWillUnmount() 
	{
		window.removeEventListener('resize', this.handleResize);
	}

	resetMargin = () => 
	{
		if (this.slider && this.sliderContent) 
		{
			this.moveSlider(0);
		}
	}

	moveSlider(ml)
	{
		this.sliderContent.scrollLeft = ml;
		this.toggleArrows();
	}

	toggleArrows()
	{
		if(this.leftArrow)
		{
			if(this.sliderContent.scrollLeft !== 0)
			{
				this.leftArrow.classList.remove('d-none');
				this.leftArrow.classList.add('d-block');
			}
			else
			{
				this.leftArrow.classList.remove('d-block');
				this.leftArrow.classList.add('d-none');
			}
		}

		if(this.rightArrow)
		{
			if(this.calcRemainingWidth() > 0)
			{
				this.rightArrow.classList.remove('d-none');
				this.rightArrow.classList.add('d-block');
			}
			else
			{
				this.rightArrow.classList.remove('d-block');
				this.rightArrow.classList.add('d-none');
			}
		}
	}

	handleLeftClicked = (toMove = null) => 
	{
		const currentMarginLeft = this.sliderContent.scrollLeft;
		const sliderWidth = toMove || this.slider.offsetWidth;
		
		let marginLeft = 0;
		if (currentMarginLeft > sliderWidth) 
		{
			marginLeft = currentMarginLeft - sliderWidth;
		} 

		this.moveSlider(marginLeft);
	}

	handleRightClicked = (toMove = null) => 
	{
		const currentMarginLeft = this.sliderContent.scrollLeft;
		const sliderWidth = toMove || this.slider.offsetWidth;
		const remainingWidth = this.calcRemainingWidth();
		
		let marginLeft = currentMarginLeft;
		if (remainingWidth > 0) 
		{
			if (remainingWidth <= sliderWidth) 
			{
				marginLeft = currentMarginLeft + remainingWidth;
			} 
			else 
			{
				marginLeft = currentMarginLeft + sliderWidth;
			}
		} 

		this.moveSlider(marginLeft);
	}

	handleResize = () => 
	{
		this.toggleArrows();
	}

	calcRemainingWidth()
	{
		const currentMarginLeft = this.sliderContent ? this.sliderContent.scrollLeft: 0;
		const sliderWidth = this.slider ? this.slider.offsetWidth : 0;
		const contentWidth = this.sliderContent ? this.sliderContent.scrollWidth : 0;
		const remainingWidth = contentWidth - sliderWidth - currentMarginLeft;

		return remainingWidth;
	}

	handleSwipe = (event) =>
	{
		switch(event.dir)
		{
		case 'Left':
			this.handleRightClicked(event.absX);
			break;
		case 'Right':
			this.handleLeftClicked(event.absX);
			break;
		}
	}

	render()
	{
		return (
			<div 
				ref={el => this.slider = el}
				className={`panel-horz-slider ${this.props.className}`}>
				<Swipeable 
					onSwiping={this.handleSwipe}
					trackMouse
					onMouseEnter={this.handleResize}
					className="panel-horz-slider-container">
					<div
						ref={el => this.sliderContent = el}
						className="panel-horz-slider-content">
						{this.props.children}
					</div>
				</Swipeable>
				<button 
					ref={el => this.leftArrow = el}
					className={`caret caret-left ${this.marginLeft !== 0? 'd-block': 'd-none'}`}
					onClick={() => this.handleLeftClicked()}>
					<i className="la la-caret-left" />
				</button>
				<button
					ref={el => this.rightArrow = el}
					className={`caret caret-right ${this.calcRemainingWidth() > 0? 'd-block': 'd-none'}`}
					onClick={() => this.handleRightClicked()}>
					<i className="la la-caret-right" />
				</button>
			</div>
		);
	}
}
