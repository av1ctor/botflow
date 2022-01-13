import React from 'react';
import PropTypes from 'prop-types';
import {Responsive, WidthProvider} from 'react-grid-layout';
import AutoSizer from 'react-virtualized-auto-sizer';
import AvatarPicker from '../../../components/Picker/AvatarPicker';
import IconPicker from '../../../components/Picker/IconPicker';
import Logs from './Logs';

import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';

const ResponsiveGridLayout = WidthProvider(Responsive);

const Dashboard = ({api, history, container}) =>
{
	const layouts = {
		xxs: [
			{i: 'a', x: 0, y: 1, w: 1, h: 1},
			{i: 'b', x: 0, y: 2, w: 1, h: 1},
			{i: 'c', x: 0, y: 3, w: 1, h: 1},
			{i: 'd', x: 0, y: 4, w: 1, h: 1},
		],		
		md: [
			{i: 'a', x: 0, y: 0, w: 1, h: 1},
			{i: 'b', x: 1, y: 0, w: 1, h: 1},
			{i: 'c', x: 0, y: 1, w: 1, h: 1},
			{i: 'd', x: 1, y: 1, w: 1, h: 1},
		]
	};

	const item = (key, title, compo) =>
		<div key={key} className="rf-dashboard-item">
			<div className="rf-dashboard-item-title">
				{title}
			</div>
			{compo}
		</div>;

	const withResizer = (children) =>
		<AutoSizer>
			{({width, height}) => 
				children(width, height)
			}
		</AutoSizer>;
	
	return (
		<>
			<div className="rf-page-header pt-2">
				<div>
					<div className="rf-page-title-icon">
						<i className="la la-cog" />
					</div>
					<div className="rf-page-title-other">
						<div className="row">
							<div className="col-auto rf-page-title-container">
								<div className="rf-page-title">
									Dashboard
								</div>
							</div>
						</div>
						<div className="row">
							<div className="col">
								<small>Página inicial</small>
							</div>
						</div>
					</div>
				</div>
			</div>

			<ResponsiveGridLayout 
				className="layout"
				breakpoints={{md: 996, xxs: 0}} 
				layouts={layouts} 
				cols={{md: 2, xxs: 1}}
				rowHeight={400} 
				draggableHandle=".rf-dashboard-item-title">
				{item('a', 'Logs', <Logs api={api} history={history} container={container} />)}
				{item('b', 'Avatares', withResizer((width, height) => <AvatarPicker width={width} height={height-40} />))}
				{item('c', 'Ícones', withResizer((width, height) => <IconPicker width={width} height={height-40} />))}
				{item('d', 'Bar', <div>...</div>)}
			</ResponsiveGridLayout>				
		</>
	);
};

Dashboard.propTypes = 
{
	api: PropTypes.object,
	history: PropTypes.object,
	container: PropTypes.object
};

export default Dashboard;