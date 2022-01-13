import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';

export default class Conditional extends PureComponent
{
	static propTypes = 
	{
		condition: PropTypes.bool,
		component: PropTypes.any,
		children: PropTypes.arrayOf(PropTypes.element)
	};

	render()
	{
		const {condition, component: C, children, ...rest} = {...this.props};
		
		if(condition && C)
		{
			if(children)
			{
				return (
					<C {...rest}>
						{children}
					</C>
				);
			}
			else
			{
				return <C {...rest} />;
			}
		}
		else
		{
			if(children)
			{
				return (
					<>
						{children}
					</>
				);
			}
			else
			{
				return null;
			}
		}
	}
}