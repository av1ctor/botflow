import React from 'react';
import PropTypes from 'prop-types';
import FormViewer from '../../Collections/Forms/Viewer/Form';

const Form = (props) =>
{
	const elm = props.element;

	return (
		<div 
			className="rf-screen-form"
			style={{
				height: elm.height,
			}}>
			<FormViewer
				api={props.api}
				container={props.container}
				collectionId={elm.collection}
				formId={elm.form}
			/>
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