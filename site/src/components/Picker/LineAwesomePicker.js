import React, {useEffect, useRef, useState} from 'react';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import {FixedSizeGrid as Grid} from 'react-window';

import './IconPicker.css';

const COL_WIDTH = 90;
const ROW_HEIGHT = 60;
const PADDING = 4;
const ICONS_CNT = 1660;

let allIcons = null;

const loadIcons = async (api) =>
{
	const res = await api.get(`misc/icons?page=0&size=${ICONS_CNT}`);
	allIcons = !res.errors?
		res.data:
		[];

	return allIcons;
};

const LineAwesomePicker = props =>
{
	const [icons, setIcons] = useState(allIcons);
	const colsPerRow = useRef(0);
	const grid = useRef();

	useEffect(() =>
	{
		(async () =>
		{
			if(!icons)
			{
				setIcons(await loadIcons(props.api));
			}
			
			if(props.selected && icons && icons.length > 0)
			{
				scrollTo(props.selected);
			}
		})();
	}, []);

	useEffect(() =>
	{
		if(props.selected && icons && icons.length > 0)
		{
			grid.current.forceUpdate();
			scrollTo(props.selected);
		}
	}, [props.selected, icons]);

	const scrollTo = (icon) =>
	{
		if(icon)
		{
			const index = icons.indexOf(icon);
			if(index)
			{
				const columnIndex = Math.floor(index % colsPerRow.current);
				const rowIndex = Math.floor(index / colsPerRow.current);
				grid.current.scrollToItem({columnIndex, rowIndex});
			}
		}
	};

	const renderIcon = ({columnIndex, rowIndex, style}) => 
	{
		const index = rowIndex * colsPerRow.current + columnIndex;

		const icon = icons[index];

		const classes = classNames(
			'rf-iconpckr-cell', 
			{'selected': icon === props.selected}, 
			{'last': columnIndex === colsPerRow.current-1});

		return (
			<div 
				className={classes} 
				style={style} 
				onClick={() => props.onChange(icon)}>
				<div className="rf-iconpckr-icon">
					<i className={`la la-${icon}`}/>
				</div>
				<div className="rf-iconpckr-name">
					{icon}
				</div>
			</div>
		);
	};

	if(!icons)
	{
		return null;
	}

	const {width, height, dir} = props;

	let rowCount = 0;
	switch(dir)
	{
	case 'horz':
		colsPerRow.current = icons.length;
		rowCount = 1;
		break;
	case 'vert':
		colsPerRow.current = 1;
		rowCount = icons.length;
		break;
	default:
		colsPerRow.current = Math.floor((width-PADDING*2)/COL_WIDTH);
		rowCount = Math.ceil(icons.length/colsPerRow.current);
		break;
	}

	return (
		<div 
			className="rf-iconpckr-container" 
			style={{width: width, height: height}}>
			<Grid
				ref={grid}
				columnCount={colsPerRow.current}
				columnWidth={COL_WIDTH}
				height={height-PADDING*2}
				rowCount={rowCount}
				rowHeight={ROW_HEIGHT}
				width={width-PADDING*2}>
				{renderIcon}
			</Grid>
		</div>
	);
};

LineAwesomePicker.propTypes = {
	api: PropTypes.object,
	width: PropTypes.number,
	height: PropTypes.number,
	dir: PropTypes.string,
	selected: PropTypes.string,
	onChange: PropTypes.func
};

LineAwesomePicker.defaultProps = {
	width: COL_WIDTH+PADDING*2,
	height: ROW_HEIGHT+PADDING*2+24,
	dir: 'both',
	onChange: () => null
};

export default LineAwesomePicker;