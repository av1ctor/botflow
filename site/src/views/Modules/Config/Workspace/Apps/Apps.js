import React, {useCallback, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Button, Col, Row, Label, Input, Card, Badge} from 'reactstrap';
import AutoSizer from 'react-virtualized-auto-sizer';
import IconPicker from '../../../../../components/Picker/IconPicker';
import BaseList from '../../../../../components/BaseList';
import BaseForm from '../../../../../components/BaseForm';
import Misc from '../../../../../libs/Misc';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import Bars from './Bars/Panel';
import BarElementHelper from './Bars/Elements/Helper';
import Screens from './Screens/Panel';

const _genScreenTitle = (screens, base = 'Screen') =>
{
	let i = screens.length;
	let title = '';
	do
	{
		const num = '000' + i;
		++i;
		title = base + (i > 0? num.substr(num.length-3): '');
	} while(screens.find(r => r.title.toLowerCase() === title.toLowerCase()));

	return title;
};

const generateScreen = (screens) =>
{
	return {
		id: Misc.genId(),
		title: _genScreenTitle(screens || []),
		elements: [
			{
				type: 'block',
				id: Misc.genId(),
				elements: [
					{
						type: 'text',
						id: Misc.genId(),
						widths: {
							xs: 12,
							md: 12
						},
						height: {value: 4, unit: 'rem'},
						text: '',
						align: 'center',
						justify: 'center',
					},
				]
			},
			{
				type: 'block',
				id: Misc.genId(),
				elements: [
					{
						type: 'form',
						id: Misc.genId(),
						widths: {
							xs: 12,
							md: 12
						},
						height: {value: 30, unit: 'rem'},
						collection: '',
						form: '',
					},
				]
			},
			{
				type: 'block',
				id: Misc.genId(),
				elements: [
					{
						type: 'text',
						id: Misc.genId(),
						widths: {
							xs: 12,
							md: 12
						},
						height: {value: 4, unit: 'rem'},
						text: '',
						align: 'center',
						justify: 'center',
					}
				]
			}
		],
	};		
};

const generateBar = (type = 'top') =>
{
	return {
		type: type,
		id: Misc.genId(),
		elements: [
			BarElementHelper.buildBlock({md: 1, align: 'flex-start'}, [
				{
					type: 'menu',
					id: Misc.genId(),
					elements: [
						{
							type: 'menuitem',
							id: Misc.genId(),
							icon: 'user',
							title: 'Logout',
							onClick: {action: 'logout', target: ''}
						}
					]
				}
			]),
			BarElementHelper.buildBlock({md: 9, align: 'center'}, [
				{
					type: 'empty',
					id: Misc.genId(),
				}
			]),
			BarElementHelper.buildBlock({md: 2, align: 'flex-end'}, [
				{
					type: 'button',
					id: Misc.genId(),
					icon: 'settings',
					title: 'Config',
					onClick: {action: '', target: ''}
				},
				{
					type: 'button',
					id: Misc.genId(),
					icon: 'user',
					title: 'Account',
					onClick: {action: '', target: ''}
				}
			])
		]
	};
};

const transformHeight = (height) =>
{
	if(!height)
	{
		return {value: 1, unit: 'rem'};
	}

	const m = height.match(/(\d+)(.*)/);
	return {value: m[1]|0, unit: m[2]};
};

const transformElementForEditing = (elm) =>
{
	return {
		...elm,
		id: Misc.genId(),
		height: elm.height?
			transformHeight(elm.height):
			undefined,
	};
};

const transformElementsForEditing = (elements) =>
{
	return elements.map(elm => ({
		...transformElementForEditing(elm), 
		elements: elm.elements? 
			transformElementsForEditing(elm.elements): 
			undefined, 
	}));
};

const transformScreenForEditing = (screen) =>
{
	return {
		...screen,
		elements: transformElementsForEditing(screen.elements)
	};
};

const transformScreensForEditing = (screens) =>
{
	return screens.map((screen) => 
		transformScreenForEditing(screen));
};

const transformBarForEditing = (bar) =>
{
	return {
		...bar,
		elements: transformElementsForEditing(bar.elements)
	};
};

const transformBarsForEditing = (bars) =>
{
	return bars.map((bar) => 
		transformBarForEditing(bar));
};

const transformForEditing = (item) =>
{
	const schemaObj = JSON.parse(item.schema);
	return {
		...item,
		schemaObj: {
			...schemaObj,
			screens: transformScreensForEditing(schemaObj.screens),
			bars: schemaObj.bars?
				transformBarsForEditing(schemaObj.bars):
				[],
		},
		createdAt: Misc.stringToDate(item.createdAt),
		updatedAt: Misc.stringToDate(item.updatedAt),
	};
};

const transformElementForSubmitting = (elm) =>
{
	return {
		...elm,
		height: elm.height && elm.height.value? 
			elm.height.value + elm.height.unit:
			undefined,
		id: undefined,
	};
};

const transformElementsForSubmitting = (elements) =>
{
	return elements.map(elm => ({
		...transformElementForSubmitting(elm), 
		elements: elm.elements? 
			transformElementsForSubmitting(elm.elements): 
			undefined, 
	}));
};

const transformScreenForSubmitting = (screen) =>
{
	return {
		...screen,
		elements: transformElementsForSubmitting(screen.elements)
	};
};

const transformScreensForSubmitting = (screens) =>
{
	return screens.map((screen) => 
		transformScreenForSubmitting(screen));
};

const transformBarForSubmitting = (bar) =>
{
	return {
		...bar,
		elements: transformElementsForSubmitting(bar.elements)
	};
};

const transformBarsForSubmitting = (bars) =>
{
	return bars.map((bar) => 
		transformBarForSubmitting(bar));
};

const transformForSubmitting = (item) =>
{
	const schemaObj = {
		...item.schemaObj,
		screens: transformScreensForSubmitting(item.schemaObj.screens),
		bars: transformBarsForSubmitting(item.schemaObj.bars)
	};

	return {
		...item,
		schema: JSON.stringify(schemaObj),
		schemaObj: undefined,
	};
};

const Apps = observer((props) =>
{
	const [app, setApp] = useState(null);
	const [onChange, setOnChange] = useState(null);
	const [screensVisible, setScreensVisible] = useState(false);
	const [barsVisible, setBarsVisible] = useState(false);

	const baseList = useRef();

	const handleItemsLoaded = useCallback((items) =>
	{
		return items.map(item => transformForEditing(item));
	}, []);
	
	const handleDelete = (item) =>
	{

	};

	const handlePreview = (item) =>
	{
		const url = CollectionUtil.genAppUrl(item.id);
		window.open(url, '_blank');
	};

	const handleShowBars = (item, onChange) =>
	{
		setApp(item);
		setOnChange(() => onChange);
		setBarsVisible(true);
	};

	const handleCloseBars = useCallback(() =>
	{
		setBarsVisible(false);
	}, [barsVisible]);

	const handleChangeBars = useCallback((bars) =>
	{
		setApp(app =>
		{
			const to = Object.assign({}, app);
			to.schemaObj.bars = bars;

			onChange({
				target: {
					name: 'schemaObj.bars',
					value: bars
				}
			});

			return to;
		});
	}, [onChange]);

	const handleShowScreens = (item, onChange) =>
	{
		setApp(item);
		setOnChange(() => onChange);
		setScreensVisible(true);
	};

	const handleCloseScreens = useCallback(() =>
	{
		setScreensVisible(false);
	}, [screensVisible]);

	const handleChangeScreens = useCallback((screens) =>
	{
		setApp(app =>
		{
			const to = Object.assign({}, app);
			to.schemaObj.screens = screens;

			onChange({
				target: {
					name: 'schemaObj.screens',
					value: screens
				}
			});

			return to;
		});
	}, [onChange]);

	const createScreen = (at) =>
	{
		const screens = Misc.addToArray(app.schemaObj.screens, at, [generateScreen(app.schemaObj.screens)]);
		handleChangeScreens(screens);
	};

	const duplicateScreen = (index) =>
	{
		const screens = Array.from(app.schemaObj.screens);
		const screen = Misc.cloneObject(screens[index]);
		screen.id = Misc.genId();
		screen.title = _genScreenTitle(screens, screen.title);
		screens.push(screen);
		handleChangeScreens(screens);
	};
	
	const moveScreen = (from, to) =>
	{
		const screens = Misc.moveArray(app.schemaObj.screens, from, to);
		handleChangeScreens(screens);
	};

	const deleteScreen = (index) =>
	{
		const screens = Misc.deleteFromArray(app.schemaObj.screens, index, 1);
		handleChangeScreens(screens);
	};

	const createBar = (at, type) =>
	{
		const bars = Misc.addToArray(app.schemaObj.bars, at, [generateBar(type)]);
		handleChangeBars(bars);
	};

	const deleteBar = (index) =>
	{
		const bars = Misc.deleteFromArray(app.schemaObj.bars, index, 1);
		handleChangeBars(bars);
	};

	const validateFields = (item, errors) =>
	{
		if(item.schemaObj.screens.length === 0)
		{
			errors.push('At least one screen must be defined');
		}
		
		return errors.length === 0;
	};

	const getItemTemplate = () =>
	{
		return {
			id: null,
			title: '',
			desc: '',
			icon: '',
			active: true,
			options: 0,
			schema: '',
			schemaObj: {
				bars: [],
				screens: [],
			},
		};
	};

	const getFilters = () =>
	{
		return [
			{name: 'id', title: 'Id', type: 'text'},
			{name: 'title', title: 'Title', type: 'text'},
		];
	};

	const getHeader = () =>
	{
		return [
			{name: 'title', title: 'Title', size: {xs:12, md:6}, klass: ''},
			{name: 'createdAt', title: 'Date', size: {md:6}, klass: 'd-none d-md-block'},
		];
	};

	const renderFields = (item, onChange) =>
	{
		const adding = !item.id;

		return (
			<>
				{!adding && 
					<Row>
						<Col xs="12">
							<Label className="base-form-label">Id</Label>
							<Input 
								disabled={true} 
								required 
								name="" 
								type="text" 
								value={item.id || ''} 
								readOnly />
						</Col>
					</Row>
				}
				<Row>
					<Col xs="12" md="6">
						<Label className="base-form-label">Title</Label>
						<Input 
							required 
							name="title" 
							type="text" 
							value={item.title || ''} 
							onChange={onChange} />
					</Col>
					<Col xs="12" md="6">
						<Label className="base-form-label">Description</Label>
						<Input 
							required 
							name="desc" 
							type="text" 
							value={item.desc || ''} 
							onChange={onChange} />
					</Col>
				</Row>
				<Row>
					<Col xs="12" md="6">
						<Label className="base-form-label">Active</Label>
						<Input 
							name="active" 
							type="checkbox" 
							className="base-form-checkbox"
							checked={item.active? true: false} 
							onChange={onChange} />
					</Col>
				</Row>
				<Row>
					<Col xs="12">
						<Label className="base-form-label">Icon</Label>
						<div style={{width: '100%', height: 92}}>
							<AutoSizer>
								{({width}) => 
									<IconPicker
										dir="horz"
										width={width}
										selected={item.icon}
										onChange={(icon) => onChange({target: {name: 'icon', value: icon}})}
									/>
								}
							</AutoSizer>
						</div>
					</Col>
				</Row>
				{!adding &&
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">URL</Label>
							<Input 
								type="text"
								value={CollectionUtil.genAppUrl(item.id)}
								readOnly
							/>
						</Col>
					</Row>
				}

				{item.createdAt &&
					<Row>
						<Col xs="12" md="6" lg="6">
							<Label className="base-form-label">Created at</Label>
							<Input 
								disabled={true} 
								type="text" 
								value={Misc.dateToString(item.createdAt) || ''} 
								readOnly />
						</Col>
						<Col xs="12" md="6" lg="6">
							<Label className="base-form-label">Created by</Label>
							<Input 
								disabled={true} 
								type="email" 
								value={item.createdBy? item.createdBy.email: ''} 
								readOnly />
						</Col>
					</Row>
				}	
				{item.updatedAt &&
					<Row>
						<Col xs="12" md="6" lg="6">
							<Label className="base-form-label">Updated at</Label>
							<Input 
								disabled={true} 
								type="text" 
								value={Misc.dateToString(item.updatedAt) || ''} 
								readOnly />
						</Col>
						<Col xs="12" md="6" lg="6">
							<Label className="base-form-label">Updated by</Label>
							<Input 
								disabled={true} 
								type="email" 
								value={item.updatedBy? item.updatedBy.email: ''} 
								readOnly />
						</Col>
					</Row>
				}
				<Label className="base-form-label">Components</Label>
				<Card className="mt-1">
					<Row>
						<Col xs="12" md="4">
							<Button 
								color="secondary" 
								block 
								className="m-1" 
								title="Edit page's toolbars"
								onClick={() => handleShowBars(item, onChange)}>
								<i className="la la-bars"/> Toolbars <small><Badge pill color="info">{item.schemaObj.bars.length}</Badge></small>
							</Button>
						</Col>
						<Col xs="12" md="4">
							<Button 
								color="secondary" 
								block 
								className="m-1" 
								title="Edit page's screens"
								onClick={() => handleShowScreens(item, onChange)}>
								<i className="la la-mobile-alt"/> Screens <small><Badge pill color="info">{item.schemaObj.screens.length}</Badge></small>
							</Button>
						</Col>							
					</Row>
				</Card>

				{!adding &&
					<>
						<Row>
							<Col xs="12">
								<hr />
							</Col>
						</Row>
						<Row>
							<Col xs="4" md="2">
								Actions:
							</Col>
							{!adding && 
								<Col xs="12" md="2">
									<Button 
										color="secondary" 
										block 
										className="m-1" 
										onClick={() => handlePreview(item)}>
										View
									</Button>
								</Col>
							}
							<Col xs="12" md="2">
								<Button 
									color="danger" 
									block 
									className="m-1" 
									onClick={() => handleDelete(item)}>
									Delete
								</Button>
							</Col>
						</Row>
					</>
				}
			</>
		);
	};

	const renderItem = (item, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="12" md="6">
						<span className="rf-list-icon"><i className={`inter-${item.icon}`}/></span>&nbsp;
						<span>{item.title}</span>
					</Col>
					<Col md="6" className="d-none d-md-block">
						{Misc.dateToString(item.createdAt)}
					</Col>
				</>
			);
		}

		const key = item.id || -1;

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<BaseForm
							key={key} 
							api={props.api}
							history={null}
							parent={baseList.current}
							controller='apps'
							item={item}
							onLoad={transformForEditing}
							render={renderFields} 
							validate={validateFields} 
							onSubmit={transformForSubmitting}
						/>
					</Col>
				</Row>
			</Col>
		);	
	};

	const renderActions = (item) =>
	{
		return (
			<>
				<Button
					size="xs" outline className="base-list-button" 
					onClick={() => handlePreview(item)} 
					title="View">
					<i className="base-list-button-add-icon la la-binoculars"></i>
				</Button>
				<Button
					size="xs" outline className="base-list-button" 
					onClick={() => handleDelete(item)} 
					title="Delete">
					<i className="base-list-button-add-icon la la-trash"></i>
				</Button>
			</>
		);
	};

	const container = useRef({});
	Object.assign(container.current, props.container, {
		createBar,
		deleteBar,
		createScreen,
		duplicateScreen,
		moveScreen,
		deleteScreen,
	});

	return (
		<div className="rf-page-contents">
			<div className="rf-page-header pt-2">
				<div>
					<div className="rf-page-title-icon">
						<i className="la la-file-word" />
					</div>
					<div className="rf-page-title-other">
						<div className="row">
							<div className="col-auto rf-page-title-container">
								<div className="rf-page-title">
									Config / Workspace / Apps
								</div>
							</div>
						</div>
						<div className="row">
							<div className="col">
								<small>The complete list of workspace&apos;s apps</small>
							</div>
						</div>
					</div>
				</div>
			</div>

			<BaseList
				ref={baseList}
				api={props.api}
				history={props.history}
				container={props.container}
				title="Apps"
				controller="apps"
				sortBy='createdAt'
				ascending={false} 
				itemTemplate={getItemTemplate()}
				getFilters={getFilters}
				getHeader={getHeader}
				onLoad={handleItemsLoaded}
				renderItem={renderItem}
				renderActions={renderActions}
			/>

			{app && app.schemaObj &&
				<Bars 
					api={props.api}
					container={container.current}
					app={app}
					screens={app.schemaObj.screens}
					bars={app.schemaObj.bars}
					visible={barsVisible}
					onHide={handleCloseBars}
					onChange={handleChangeBars}
				/>
			}

			{app && app.schemaObj &&
				<Screens 
					api={props.api}
					container={container.current}
					app={app}
					screens={app.schemaObj.screens}
					visible={screensVisible}
					onHide={handleCloseScreens}
					onChange={handleChangeScreens}
				/>
			}
		</div>
	);
});

Apps.propTypes = 
{
	api: PropTypes.object.isRequired,
	history: PropTypes.object,
	container: PropTypes.object.isRequired
};

export default Apps;
