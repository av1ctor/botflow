import React, {createContext} from 'react';
import PropTypes from 'prop-types';

export const SessionContext = createContext();

export const SessionContextProvider = ({store, children}) =>
{
	return (
		<SessionContext.Provider value={store}>
			{children}
		</SessionContext.Provider>
	);
};

SessionContextProvider.propTypes = {
	children: PropTypes.any,
	store: PropTypes.object,
};
