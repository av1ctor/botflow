import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';

export default class DocPreview 
	extends PureComponent
{
	static propTypes = {
		url: PropTypes.string,
		provider: PropTypes.string,
		width: PropTypes.any.isRequired,
		height: PropTypes.any.isRequired,
	};

	static defaultProps = {
		provider: 'INTERNO'
	};

	componentDidMount()
	{
		this.show(this.props.url);
	}
	
	componentDidUpdate(prevProps)
	{
		if(prevProps.url !== this.props.url)
		{
			this.show(this.props.url);
		}
	}
	
	async show(url)
	{
		if(!url)
		{
			return;
		}

		switch(this.props.provider)
		{
		case 'DROPBOX':
			this.elm.href = url;
			window.Dropbox.embed({appKey: process.env.REACT_APP_ROBOTIKFLOW_OAUTH2_DROPBOX_CLIENTID, link: url}, this.elm);
			break;

		default:
			this.elm.src = url;
			break;
		}
	}

	render()
	{
		if(!this.props.url)
		{
			return null;
		}

		switch(this.props.provider)
		{
		case 'DROPBOX':
			return (
				<a
					ref={el => this.elm = el}
					href={this.props.url}
					className="dropbox-embed"
					data-height={this.props.width}
					data-width={this.props.height}>
				</a>
			);

		default:
			return (
				<iframe
					ref={el => this.elm = el}
					width={this.props.width}
					height={this.props.height}
					sandbox="allow-scripts allow-forms allow-same-origin"
					style={{border: 'none', margin: '0 auto', display: 'block'}}
				/>
			);
		}
	}
}