import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Card, CardBody, CardHeader, Row, Col} from 'reactstrap';
import Chart from 'chart.js';
import Misc from '../../libs/Misc';
import palette from 'google-palette';

const colorSet = Misc.shuffleArray(palette('tol-rainbow', 128), 'chart');

export default class ChartPanel extends PureComponent
{
	static propTypes = 
	{
		type:  PropTypes.string.isRequired,
		labels: PropTypes.any.isRequired,
		datasets: PropTypes.any.isRequired,
		width: PropTypes.any.isRequired,
		height: PropTypes.any.isRequired,
		isOpen: PropTypes.bool
	};

	constructor(props)
	{
		super(props);

		this.state = {
			isOpen: props.isOpen? true: false,
		};

		this.labels = null;
		this.datasets = null;
		this.chart = null;
		this.canvas = null;

		this.handleCollapse = this.handleCollapse.bind(this);
	}

	componentDidMount()
	{
		const labels = this.transformLabels(this.props);
		const datasets = this.transformDatesets(this.props);
		this.createChart(labels, datasets);
	}

	componentDidUpdate(prevProps)
	{
		const labels = this.transformLabels(this.props);
		const datasets = this.transformDatesets(this.props);
			
		if(this.props.type !== prevProps.type ||
			!Misc.compareArrays(labels, this.labels) || 
			!Misc.compareArrays(datasets, this.datasets))
		{
			this.chart.destroy();
			this.chart = null;
			
			this.createChart(labels, datasets);
		}
	}

	handleCollapse()
	{
		this.setState({
			isOpen: !this.state.isOpen
		});
	}

	createChart(labels, datasets) 
	{
		const numFormat = (num) => Misc.round(num);
		
		if (this.chart !== null || this.canvas === null) 
		{
			return;
		}

		this.labels = labels;
		this.datasets = datasets;

		this.chart = new Chart(this.canvas, {
			type: this.props.type,
			data: {
				labels: Array.from(this.labels),
				datasets: this.datasets.map(ds => Misc.cloneObject(ds))
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				legend: { display: true },
				scales: {
					yAxes: this.datasets.map((ds, index) => ({
						ticks: {
							beginAtZero: true,
							id: 'y-axis-' + index,
							callback: function(value) {
								return numFormat(value);
							}							
						}
					}))
				},
				tooltips: {
					callbacks: {
						label: function(tooltipItem, chart){
							var datasetLabel = chart.datasets[tooltipItem.datasetIndex].label || '';
							return datasetLabel + ': ' + numFormat(tooltipItem.yLabel, 2);
						}
					}
				}					
			}
		});
	}

	transformLabels(props)
	{
		return props.labels.constructor === Function? props.labels(): props.labels;
	}

	transformDatesets(props)
	{
		const datasets = props.datasets.constructor === Function? props.datasets(): props.datasets;

		return datasets.map((ds, index) => ({
			label: ds.label,
			data: ds.data,
			backgroundColor: this.props.type !== 'line'? 
				'#' + colorSet[index & 127]: 
				null,
			fill: props.type !== 'line',
			borderColor: 'rgba(0, 0, 0, 0.1)',
			yAxisID: 'y-axis-' + index
		}));
	}

	render()
	{
		return(
			<Card className="rf-col-report-chart-container">
				<CardHeader className="rf-col-report-chart-container-title">
					<Row>
						<Col xs="12" className="pl-4 pt-2" onClick={this.handleCollapse}>
							<strong>Gráfico</strong>
						</Col>
					</Row>
				</CardHeader>
				<CardBody>
					<Row>
						<div style={{width: this.props.width, height: this.props.height}}>
							<canvas ref={el => this.canvas = el} />
						</div>
					</Row>
				</CardBody>
			</Card>
		);
	}
}

