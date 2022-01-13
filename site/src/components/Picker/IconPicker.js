import React, {PureComponent} from 'react';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import {FixedSizeGrid as Grid} from 'react-window';

import './IconPicker.css';

const COL_WIDTH = 90;
const ROW_HEIGHT = 60;
const PADDING = 4;

export default class IconPicker extends PureComponent
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
			'alarm-clock','antenna','apartment','shopping-bag','shopping-basket','shopping-basket-1','battery','battery-1','bell','binocular','sailboat','book','bookmark',
			'briefcase','brightness','browser','paint-brush','building','idea','school-bus','birthday-cake','birthday-cake-1','calculator','calendar','calendar-1','calendar-2',
			'shopping-cart','money','money-1','money-2','cctv','certificate','certificate-1','chair','chat','chat-1','chef','cursor','wall-clock','coding','coffee','coffee-1',
			'compass','computer','computer-1','agenda','crop','crown','pendrive','calendar-3','calendar-4','ruler','diagram','diamond','book-1','chat-2','chat-3','route','file',
			'inbox','download','cocktail','dumbbell','dvd','edit','edit-1','edit-2','mortarboard','calendar-5','calendar-6','factory','file-1','file-2','filter','filter-1',
			'fire-extinguisher','flag','flame','flash','flight','flight-1','bottle','floppy-disk','flow','focus','folder','dinner','fuel','gamepad','gift','trolley','package',
			'hammer','hammer-1','headset','house','house-1','hook','id-card','id-card-1','idea-1','image','image-1','image-2','inbox-1','inbox-2','inbox-3','inbox-4','download-1',
			'bug','invoice','invoice-1','key','startup','startup-1','library','idea-2','lighthouse','link','pin','pin-1','padlock','magic-wand','magnifying-glass','email','email-1',
			'map','pin-2','map-1','marker','first-aid-kit','mail','chat-4','email-2','chip','chip-1','microphone','microphone-1','smartphone','cocktail-1','more','ticket','compass-1',
			'add-file','nib','notebook','notepad','notepad-1','notepad-2','notification','notification-1','open-book','open-book-1','file-3','paint-bucket','paint-roller','paper-plane',
			'pen','pencil','pencil-1','smartphone-1','photo-camera','push-pin','pin-3','push-pin-1','push-pin-2','video-player','swimming-pool','presentation','presentation-1',
			'presentation-2','file-4','user','property','wallet','radio','radio-1','random','open-book-2','reload','cutlery','startup-2','router','ruler-1','safebox','hourglass',
			'satellite','calendar-7','monitor','monitor-1','search','cursor-1','settings','share','share-1','share-2','crane','ship','shopping-cart-1','sim-card','sofa','speaker',
			'speaker-1','speech','stamp','stethoscope','suitcase','syringe','tag','tag-1','target','tea','chip-2','telescope','ticket-1','ticket-2','calendar-8','torch','train',
			'delivery-truck','delivery-truck-1','delivery-truck-2','trash','suitcase-1','television','umbrella','outbox','upload','usb','user-1','video-camera','gallery','film-reel',
			'video-player-1','wallet-1','watch','bottle-1','coding-1','wifi','writing','zoom-in','zoom-out'
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
			<div className={classes} style={style} onClick={() => this.props.onChange(icon)}>
				<div className="rf-iconpckr-icon">
					<i className={`inter-${icon}`}/>
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
			<div className="rf-iconpckr-container" style={{width: width, height: height}}>
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