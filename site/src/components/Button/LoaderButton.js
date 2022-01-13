import React from 'react';
import {Button} from 'reactstrap';
import './LoaderButton.css';

const LoaderButton = ({
	isLoading,
	text,
	loadingText,
	className = '',
	disabled = false,
	...props
}) =>
	<Button
		className={`LoaderButton ${className}`}
		disabled={disabled || isLoading}
		{...props}
	>
		{isLoading && <div className="spinning" />}
		{!isLoading ? text : loadingText}
	</Button>;


export default LoaderButton; 