import React, {useCallback} from 'react';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';

const GridEditor = props =>
{
	const onChange = useCallback(async (o) => 
	{
		await props.onChange({target: {value: o.value}}); 
		props.onSubmit(null);
	}, []);

	const renderContent = (o, index) => {
		const style = {
		};

		if(o.color)
		{
			style.backgroundColor = props.container.getItemCellColor(o.color, o.value);
		}
		else if(o.getColor)
		{
			style.backgroundColor = o.getColor({value: o.value});
		}

		return (
			<div 
				key={index} 
				className={`rf-col-edit-grid-col col-6 ${props.value === o.value? 'rf-col-edit-grid-col-selected': ''}`}
				style={style}
				tabIndex={index}
				data-autofocus={index === 0}
				onClick={() => onChange(o)}>
				{o.name}
			</div>
		);
	};
	
	if(!props.visible)
	{
		return null;
	}

	if(!props.list)
	{
		return null;
	}

	const list = props.list;
	let grid = [];

	for(let i = 0; i < list.length - 1; i += 2)
	{
		grid.push(
			<div key={i} className="rf-col-edit-grid-row row">
				{renderContent(list[i], i)}
				{renderContent(list[i+1], i+1)}
			</div>);
	}

	if((list.length & 1) === 1)
	{
		let i = list.length-1;
		grid.push(
			<div key={i} className="rf-col-edit-grid-row row">
				{renderContent(list[i], i)}
			</div>);
	}

	const {focus} = props;

	return(
		<FocusLock 
			group="cell-editors" 
			disabled={!props.visible} 
			autoFocus={focus}
			returnFocus={focus} 
			persistentFocus={focus}>
			{props.title &&
				<div className="row">
					<div className="col-12">
						<strong>{props.title}</strong>
					</div>
				</div>
			}
			<div className="rf-col-edit-grid">
				{grid}
			</div>
		</FocusLock>
	);
};

GridEditor.propTypes = 
{
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
	container: PropTypes.object.isRequired,
	title: PropTypes.string,
	value: PropTypes.any,
	list: PropTypes.array,
	visible: PropTypes.bool,
	focus: PropTypes.bool,
};

GridEditor.defaultProps = {
	focus: true
};

export default GridEditor;