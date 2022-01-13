import React, {useCallback} from 'react';
import {VariableSizeList} from 'react-window';
import Scrollbars from '../ScrollbarsCustom';

// eslint-disable-next-line react/prop-types
const ScrollableList = ({ onScroll, forwardedRef, style, children, ...props }) => 
{
	const refSetter = useCallback(scrollbarsRef => 
	{
		return forwardedRef(scrollbarsRef? scrollbarsRef.view: null);
	}, []);
	
	return (
		<Scrollbars
			{...props}
			ref={refSetter}
			style={{ ...style, overflow: 'hidden' }}
			onScroll={onScroll}>
			{children}
		</Scrollbars>
	);
};

const VariableSizeListScrollable = (props) =>
{
	// eslint-disable-next-line react/prop-types, react/display-name
	const renderOuterContainer = React.forwardRef((props, ref) => 
	{
		return (
			<ScrollableList 
				{...props} 
				forwardedRef={ref} 
			/>
		);
	});
  
	return (
		<VariableSizeList
			{...props}
			ref={props.setListRef}
			outerElementType={renderOuterContainer} 
		/>
	);
};

export default VariableSizeListScrollable;