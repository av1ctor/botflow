import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';

export default class Button extends PureComponent 
{
	static propTypes = 
	{
		icon: PropTypes.string,
		title: PropTypes.any,
		inline: PropTypes.bool,
		children: PropTypes.any
	};
	
	render() 
	{
		const {children, icon, title, inline, ...props} = this.props;

		const renderInline = () =>
		{
			return(
				<>
					<span className="ribbon-button-title"><i className={`la la-${icon}`}/> {title}</span>
				</>
			);
		};

		const renderBlock = () =>
		{
			return(
				<>
					<div className="ribbon-button-icon">
						<i className={`la la-${icon}`}/>
					</div>
					<div className="ribbon-button-title"><div>{title}</div></div>
				</>
			);
		};
		
		return (
			<button 
				type="button" 
				className="ribbon-button btn btn-secondary btn-block" 
				{...props}>
				{children?
					children:
					inline? renderInline(): renderBlock()
				}
			</button>            
		);
	}
}
