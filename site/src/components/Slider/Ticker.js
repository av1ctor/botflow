import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';

import './Ticker.css';

export default class Ticker extends PureComponent
{
    static propTypes = 
    {
        children: PropTypes.any
    };

	render()
	{
        const children = Array.isArray(this.props.children)? this.props.children: [this.props.children];
        return (
			<div className="ticker-wrap">
				<div className="ticker">
                    {children.map((child, index) =>
                        <div key={index} className="ticker__item">
                            {child}
                        </div>
                    )}
				</div>
			</div>
		);
	}
}