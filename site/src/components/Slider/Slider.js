import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {getSlider} from 'simple-slider';

export default class Slider extends PureComponent
{
    static propTypes = 
    {
        children: PropTypes.any
    };

    constructor(props)
    {
        super(props);

        this.sliderElm = null;
        this.slider = null;
    }

    componentDidMount()
    {
        if(this.sliderElm)
        {
            this.slider = getSlider({
                container: this.sliderElm,
                delay: 10
            });
        }
    }

    componentWillUnmount()
    {
        if(this.slider)
        {
            this.slider.dispose();
            this.slider = null;
        }
    }

    render()
    {
        const {children, ...rest} = this.props;
        
        if(!children || !Array.isArray(children) || children.length === 1)
        {
            return (
                <div {...rest}>
                    {children}
                </div>
            );
        }
        
        return (
            <div 
                ref={el => this.sliderElm = el}
                {...rest}>
                {children}
            </div>
        );
    }
}
