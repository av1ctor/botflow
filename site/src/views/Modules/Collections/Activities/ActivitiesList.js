import React, {useCallback} from 'react';
import PropTypes from 'prop-types';
import ExtensibleListCollapsed from '../../../../components/List/ExtensibleListCollapsed';
import ActivityForm, {genEmptyActivity} from './ActivityForm';

export const onChangeActivityType = (e, onChange) =>
{
	onChange({
		target: {
			name: e.target.name,
			id: e.target.id,
			value: {
				...genEmptyActivity(),
				type: e.target.value, 
			}
		}
	});
};

const ActivitiesList = (props) =>
{
	const handleAddActivity = useCallback((item, onChange) =>
	{
		onChange({
			target: {
				type: 'object',
				name: 'activities',
				value: item.activities.concat([genEmptyActivity()])
			}
		});
	}, []);

	const handleDeleteActivity = useCallback((item, index, onChange) =>
	{
		onChange({
			target: {
				type: 'object',
				name: 'activities',
				value: item.activities.filter((a, i) => i !== index)
			}
		});
	}, []);

	const renderActivityPreview = ({item, index}) =>
	{
		return (
			<div key={`prev_${index}`}>
				{item.activity.schemaObj.title}
			</div>
		);
	};

	const {element, onChange} = props;

	return (
		<ExtensibleListCollapsed
			useIndex
			items={element.activities} 
			renderPreview={renderActivityPreview}
			addItem={() => handleAddActivity(element, onChange)}
			deleteItem={(item, index) => handleDeleteActivity(element, index, onChange)}
			extraAddButtonText="activity">
			{({item, index}) => 
				<ActivityForm 
					{...props}	
					index={index}
					activity={item}
					onChange={onChange}
				/>
			}
		</ExtensibleListCollapsed>		
	);
};

ActivitiesList.propTypes = {
	isMobile: PropTypes.bool,
	element: PropTypes.object.isRequired,
	activities: PropTypes.array.isRequired,
	refs: PropTypes.instanceOf(Map).isRequired,
	onChange: PropTypes.func.isRequired,
	loadRefs: PropTypes.func.isRequired,
	getFieldOptions: PropTypes.func.isRequired,
	getFunctionOptions: PropTypes.func.isRequired,
};

export default ActivitiesList;