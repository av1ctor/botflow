import React from 'react';
import {useParams} from 'react-router';
import PropTypes from 'prop-types';
import Form from './Form';

import './Form.css';

const Page = (props) =>
{
	const params = useParams();

	return (
		<div className="rf-form-page">
			<Form 
				api={props.api}	
				container={props.container}	
				collectionId={params.colId}
				formId={params.id} 
			/>
		</div>
	);
};

Page.propTypes = {
	api: PropTypes.object,
	container: PropTypes.object,
};

export default Page;