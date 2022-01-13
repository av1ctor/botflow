import React, {useCallback} from 'react';
import PropTypes from 'prop-types';
import {Controlled as CodeMirror} from 'react-codemirror2';
import 'codemirror/mode/javascript/javascript';
import 'codemirror/addon/lint/lint';
import 'codemirror/addon/lint/json-lint';
import jsonlint from '../../libs/jsonlint';

import 'codemirror/lib/codemirror.css';
import 'codemirror/addon/lint/lint.css';

window.jsonlint = jsonlint;

const JsonEditor = props =>
{
	const onBeforeChange = useCallback((editor, data, value) => 
	{
		props.handleOnChange({
			target:{
				type:'textarea', 
				value: value, 
				name: props.name
			}
		});
	}, []);

	return(
		<CodeMirror 
			options={{
				mode: {
					name: 'javascript', 
					json: true
				}, 
				lineNumbers: true, 
				indentUnit: 4, 
				indentWithTabs: true,
				gutters: ['CodeMirror-lint-markers'],
				lint: true
			}}
			onBeforeChange={onBeforeChange} 
			value={props.value} />
	);
};

JsonEditor.propTypes = 
{
	name: PropTypes.string.isRequired,
	value: PropTypes.string.isRequired,
	handleOnChange: PropTypes.func.isRequired
};

export default JsonEditor;