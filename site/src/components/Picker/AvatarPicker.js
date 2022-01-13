import React, {PureComponent} from 'react';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import {FixedSizeGrid as Grid} from 'react-window';

import './IconPicker.css';

const COL_WIDTH = 90;
const ROW_HEIGHT = 60;
const PADDING = 4;

export default class AvatarPicker extends PureComponent
{
	static propTypes = {
		width: PropTypes.number,
		height: PropTypes.number,
		dir: PropTypes.string,
		selected: PropTypes.string,
		onChange: PropTypes.func
	};

	static defaultProps = {
		width: COL_WIDTH+PADDING*2,
		height: ROW_HEIGHT+PADDING*2+24,
		dir: 'both',
		onChange: () => null
	};

	constructor(props)
	{
		super(props);

		this.icons = [
			'afro','afrofuturism','beatnik','biker','bodybuilder','bohemian','bosozoku','boy','chonga','cosplayer','cosplayer2','e-girl',
			'e-girl2','eighties','emo','formal','gang-member','ganguro-girl','gothic','gothic2','grunge','grunge2','heavy','hippie','hipster',
			'hipster2','lady','mod','mod2','modern','nerd','otaku','pimp','posh','preppy','punk','punk2','punk3','rapper','rapper2','rastafari',
			'rave','regular','regular2','rockabilly','rocker','rocker2','seapunk','surfer','yuppie'
		].sort();

		this.colsPerRow = 0;
		this.grid = null;
	}

	componentDidMount()
	{
		const props = this.props;
		if(props.selected)
		{
			this.scrollTo(props.selected);
		}
	}

	componentDidUpdate(prevProps)
	{
		const props = this.props;
		if(props.selected !== prevProps.selected)
		{
			this.grid.forceUpdate();
			this.scrollTo(props.selected);
		}
	}

	scrollTo = (icon) =>
	{
		if(icon)
		{
			const index = this.icons.indexOf(icon);
			if(index)
			{
				const columnIndex = Math.floor(index % this.colsPerRow);
				const rowIndex = Math.floor(index / this.colsPerRow);
				this.grid.scrollToItem({columnIndex, rowIndex});
			}
		}
	}

	renderIcon = ({columnIndex, rowIndex, style}) => 
	{
		const index = rowIndex * this.colsPerRow + columnIndex;

		const icon = this.icons[index];

		const classes = classNames(
			'rf-iconpckr-cell', 
			{'selected': icon === this.props.selected}, 
			{'last': columnIndex === this.colsPerRow-1});

		return (
			<div 
				className={classes} 
				style={style} 
				onClick={() => this.props.onChange(icon)}>
				<div className="rf-iconpckr-avatar">
					<i className={`ava-${icon}`}/>
				</div>
				<div className="rf-iconpckr-name">
					{icon}
				</div>
			</div>
		);
	}

	render()
	{
		const {width, height, dir} = this.props;

		let rowCount = 0;
		switch(dir)
		{
		case 'horz':
			this.colsPerRow = this.icons.length;
			rowCount = 1;
			break;
		case 'vert':
			this.colsPerRow = 1;
			rowCount = this.icons.length;
			break;
		default:
			this.colsPerRow = Math.floor((width-PADDING*2)/COL_WIDTH);
			rowCount = Math.ceil(this.icons.length/this.colsPerRow);
			break;
		}

		return (
			<div 
				className="rf-iconpckr-container" 
				style={{width: width, height: height}}>
				<Grid
					ref={el => this.grid = el}
					columnCount={this.colsPerRow}
					columnWidth={COL_WIDTH}
					height={height-PADDING*2}
					rowCount={rowCount}
					rowHeight={ROW_HEIGHT}
					width={width-PADDING*2}>
					{this.renderIcon}
				</Grid>
			</div>
		);
	}
}