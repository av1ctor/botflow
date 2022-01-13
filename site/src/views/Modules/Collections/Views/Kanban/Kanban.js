import React, {forwardRef, Fragment, memo, useState, useRef, useContext, useCallback, useEffect} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';


const Kanban = observer(props =>
{
	return (
		'not implemented :/'
	);
});

Kanban.propTypes = {
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	viewId: PropTypes.string,
	insertableFieldsCount: PropTypes.number,
};

export default Kanban;

