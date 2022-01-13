import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import SimpleMDE from 'react-simplemde-editor';
import 'easymde/dist/easymde.min.css';

export default class MarkdownEditor extends PureComponent
{
	static propTypes = {
		onChange: PropTypes.func,
		value: PropTypes.string,
		options: PropTypes.object
	};

	render()
	{
		return (
			<SimpleMDE
				onChange={this.props.onChange}
				value={this.props.value}
				options={{
					autofocus: false,
					spellChecker: false,
					status: false,
					...this.props.options
				}}
			/>
		);
	}
}



