import React, {createContext} from 'react';
import PropTypes from 'prop-types';

export const WorkspaceContext = createContext();

export const WorkspaceContextProvider = ({store, children}) =>
{
	return (
		<WorkspaceContext.Provider value={store}>
			{children}
		</WorkspaceContext.Provider>
	);
};

WorkspaceContextProvider.propTypes = {
	children: PropTypes.any,
	store: PropTypes.object,
};
