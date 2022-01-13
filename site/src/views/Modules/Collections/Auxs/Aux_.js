import React from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import Collection from '../Collection';

const Aux = observer(props =>
{
	const store = props.store;
	
	return (
		<div className="rf-col-auxs-tab">
			<Collection 
				{...props.childProps}
				store={store}
				visible={props.visible}
				isAux={true}
			/>
		</div>
	);
});

Aux.propTypes = 
{
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
	childProps: PropTypes.object,
};


export default Aux;