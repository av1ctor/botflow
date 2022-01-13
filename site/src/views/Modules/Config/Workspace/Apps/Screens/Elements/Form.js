import React, { useCallback } from 'react';
import PropTypes from 'prop-types';
import FormViewer from '../../../../../Collections/Forms/Viewer/Form';

const Form = (props) =>
{
	const elm = props.element;

	const handlePrevent = useCallback((e) => 
	{
		e.preventDefault();
		e.stopPropagation();
	}, []);

	return (
		<div 
			className="rf-screeneditor-form rf-screen-form"
			style={{
				height: elm.height.value + elm.height.unit,
			}}
			onSubmitCapture={handlePrevent}>
			{elm.collection && elm.form? 
				<FormViewer
					api={props.api}
					container={props.container}
					collectionId={elm.collection}
					formId={elm.form}
					disabled={true}
				/>:
				<div>Undefined form</div>
			}
		</div>
	);	
};

Form.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	element: PropTypes.object,
};

export default Form;