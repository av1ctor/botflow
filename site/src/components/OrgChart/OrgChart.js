import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';

import './OrgChart.css';

export default class OrgChart extends PureComponent
{
	static propTypes = {
		value: PropTypes.array.isRequired,
		nodeTemplate: PropTypes.func
	};

	static defaultProps = {
		nodeTemplate: (node) => node.label
	};

	renderNode(node)
	{
		const isRoot = !node.parent;
		const isLeaf = !node.children || node.children.length === 0 || !node.children.find(child => !child.deleted);
		const isDeleted = node.deleted;

		return (!isDeleted &&
			<li 
				key={node.id}
				className={classNames({'root': isRoot, 'leaf': isLeaf, 'node': !isRoot && !isLeaf})}>
				<div className="nodecontent">{this.props.nodeTemplate(node)}</div>
				{
					!isLeaf &&
					<ul>
						{node.children.map(child => this.renderNode(child) )}
					</ul>
				}
			</li>
		);
	}

	render()
	{
		const {value} = this.props;

		return (
			<ul className="orgchart">
				{this.renderNode(value[0])}
			</ul>
		);
	}
}