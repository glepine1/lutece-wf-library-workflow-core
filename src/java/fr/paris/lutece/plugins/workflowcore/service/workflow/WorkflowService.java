/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.workflowcore.service.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.action.ActionFilter;
import fr.paris.lutece.plugins.workflowcore.business.prerequisite.Prerequisite;
import fr.paris.lutece.plugins.workflowcore.business.resource.IResourceHistoryFactory;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceWorkflow;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.business.state.StateFilter;
import fr.paris.lutece.plugins.workflowcore.business.task.ITaskType;
import fr.paris.lutece.plugins.workflowcore.business.workflow.IWorkflowDAO;
import fr.paris.lutece.plugins.workflowcore.business.workflow.Workflow;
import fr.paris.lutece.plugins.workflowcore.business.workflow.WorkflowFilter;
import fr.paris.lutece.plugins.workflowcore.exception.WorkflowRuntimeException;
import fr.paris.lutece.plugins.workflowcore.service.action.IActionService;
import fr.paris.lutece.plugins.workflowcore.service.prerequisite.IAutomaticActionPrerequisiteService;
import fr.paris.lutece.plugins.workflowcore.service.prerequisite.IPrerequisiteManagementService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceWorkflowService;
import fr.paris.lutece.plugins.workflowcore.service.state.IStateService;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.plugins.workflowcore.service.task.ITaskFactory;
import fr.paris.lutece.plugins.workflowcore.service.task.ITaskService;

/**
 *
 * AbstractWorkflowService
 *
 */
public class WorkflowService implements IWorkflowService
{
    /**
     * Name of the bean of this service
     */
    public static final String BEAN_SERVICE = "workflow.workflowService";
    @Inject
    private ITaskFactory _taskFactory;
    @Inject
    private ITaskService _taskService;
    @Inject
    private IWorkflowDAO _workflowDAO;
    @Inject
    private IResourceWorkflowService _resourceWorkflowService;
    @Inject
    private IStateService _stateService;
    @Inject
    private IActionService _actionService;
    @Inject
    private IResourceHistoryService _resourceHistoryService;
    @Inject
    private IResourceHistoryFactory _resourceHistoryFactory;
    @Inject
    private IPrerequisiteManagementService _prerequisiteManagementService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void create( Workflow workflow )
    {
        _workflowDAO.insert( workflow );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update( Workflow workflow )
    {
        _workflowDAO.store( workflow );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove( int nIdWorkflow )
    {
        // Remove associated workflow resources
        for ( ResourceWorkflow resourceWorkflow : _resourceWorkflowService.getAllResourceWorkflowByWorkflow( nIdWorkflow ) )
        {
            doRemoveWorkFlowResource( resourceWorkflow.getIdResource( ), resourceWorkflow.getResourceType( ), nIdWorkflow );
        }

        // Remove associated actions
        ActionFilter actionFilter = new ActionFilter( );
        actionFilter.setIdWorkflow( nIdWorkflow );
        actionFilter.setAutomaticReflexiveAction( false );

        for ( Action actionToRemove : _actionService.getListActionByFilter( actionFilter ) )
        {
            _actionService.remove( actionToRemove.getId( ) );
        }

        actionFilter.setAutomaticReflexiveAction( true );
        for ( Action actionToRemove : _actionService.getListActionByFilter( actionFilter ) )
        {
            _actionService.remove( actionToRemove.getId( ) );
        }

        // Remove associated states
        StateFilter stateFilter = new StateFilter( );
        stateFilter.setIdWorkflow( nIdWorkflow );

        for ( State state : _stateService.getListStateByFilter( stateFilter ) )
        {
            _stateService.remove( state.getId( ) );
        }

        _workflowDAO.delete( nIdWorkflow );
    }

    // FINDERS

    /**
     * {@inheritDoc}
     */
    @Override
    public Workflow findByPrimaryKey( int nIdWorkflow )
    {
        return _workflowDAO.load( nIdWorkflow );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Workflow> getListWorkflowsByFilter( WorkflowFilter filter )
    {
        return _workflowDAO.selectWorkflowByFilter( filter );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Action> getActions( int nIdResource, String strResourceType, int nIdWorkflow )
    {
        List<Action> listAction = new ArrayList<>( );
        State resourceState = null;
        ResourceWorkflow resourceWorkflow = _resourceWorkflowService.findByPrimaryKey( nIdResource, strResourceType, nIdWorkflow );

        if ( resourceWorkflow != null )
        {
            resourceState = resourceWorkflow.getState( );
        }
        else
        {
            // Get initial state
            StateFilter filter = new StateFilter( );
            filter.setIsInitialState( StateFilter.FILTER_TRUE );
            filter.setIdWorkflow( nIdWorkflow );

            List<State> listState = _stateService.getListStateByFilter( filter );

            if ( CollectionUtils.isNotEmpty( listState ) )
            {
                resourceState = listState.get( 0 );
            }
        }

        if ( resourceState != null )
        {
            ActionFilter filter = new ActionFilter( );
            filter.setIdStateBefore( resourceState.getId( ) );
            filter.setIdWorkflow( nIdWorkflow );
            filter.setAutomaticReflexiveAction( false );
            listAction = _actionService.getListActionByFilter( filter );
        }

        return listAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, List<Action>> getActions( List<Integer> listIdResource, String strResourceType, Integer nIdExternalParentId, int nIdWorkflow )
    {
        Map<Integer, List<Action>> result = new HashMap<>( );
        State initialState = null;

        // Get initial state
        StateFilter filter = new StateFilter( );
        filter.setIsInitialState( StateFilter.FILTER_TRUE );
        filter.setIdWorkflow( nIdWorkflow );

        List<State> listState = _stateService.getListStateByFilter( filter );

        if ( CollectionUtils.isNotEmpty( listState ) )
        {
            initialState = listState.get( 0 );
        }

        Map<Integer, Integer> listIdsState = _resourceWorkflowService.getListIdStateByListId( listIdResource, nIdWorkflow, strResourceType,
                nIdExternalParentId );

        listIdResource.removeAll( listIdsState.keySet( ) );

        // Get all workflowstate
        StateFilter filterAll = new StateFilter( );
        filterAll.setIdWorkflow( nIdWorkflow );

        List<State> listAllState = _stateService.getListStateByFilter( filterAll );

        Map<Integer, List<Action>> listActionByStateId = new HashMap<>( );

        for ( State state : listAllState )
        {
            ActionFilter actionfilter = new ActionFilter( );
            actionfilter.setIdStateBefore( state.getId( ) );
            actionfilter.setIdWorkflow( nIdWorkflow );
            actionfilter.setAutomaticReflexiveAction( false );

            List<Action> listAction = _actionService.getListActionByFilter( actionfilter );
            listActionByStateId.put( state.getId( ), listAction );
        }

        for ( Entry<Integer, Integer> entry : listIdsState.entrySet( ) )
        {
            if ( listActionByStateId.containsKey( entry.getValue( ) ) )
            {
                result.put( entry.getKey( ), listActionByStateId.get( entry.getValue( ) ) );
            }
        }

        if ( initialState != null )
        {
            ActionFilter actionfilter = new ActionFilter( );
            actionfilter.setIdStateBefore( initialState.getId( ) );
            actionfilter.setIdWorkflow( nIdWorkflow );

            List<Action> listAction = _actionService.getListActionByFilter( actionfilter );

            for ( int nId : listIdResource )
            {
                result.put( nId, listAction );
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<State> getAllStateByWorkflow( int nIdWorkflow )
    {
        StateFilter stateFilter = new StateFilter( );
        stateFilter.setIdWorkflow( nIdWorkflow );

        return _stateService.getListStateByFilter( stateFilter );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Action> getMassActions( int nIdWorkflow )
    {
        ActionFilter aFilter = new ActionFilter( );
        aFilter.setIdWorkflow( nIdWorkflow );
        aFilter.setIsMassAction( true );
        aFilter.setAutomaticReflexiveAction( false );

        return _actionService.getListActionByFilter( aFilter );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Action> getMassActions( int nIdWorkflow, int stateId )
    {
        ActionFilter aFilter = new ActionFilter( );
        aFilter.setIdWorkflow( nIdWorkflow );
        aFilter.setIsMassAction( true );
        aFilter.setIdStateBefore( stateId );
        aFilter.setAutomaticReflexiveAction( false );

        return _actionService.getListActionByFilter( aFilter );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getState( int nIdResource, String strResourceType, int nIdWorkflow, Integer nIdExternalParent )
    {
        State resourceState = _stateService.findByResource( nIdResource, strResourceType, nIdWorkflow );

        if ( resourceState == null )
        {
            // Get initial state
            Workflow workflow = findByPrimaryKey( nIdWorkflow );
            ResourceWorkflow resourceWorkflow = null;

            if ( workflow != null )
            {
                resourceWorkflow = getInitialResourceWorkflow( nIdResource, strResourceType, workflow, nIdExternalParent );
            }

            if ( resourceWorkflow != null )
            {
                _resourceWorkflowService.create( resourceWorkflow );
                resourceState = resourceWorkflow.getState( );
                doProcessAutomaticReflexiveActions( nIdResource, strResourceType, resourceState.getId( ), nIdExternalParent, Locale.getDefault( ) );
            }
        }

        return resourceState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getMapTaskTypes( Locale locale )
    {
        Collection<ITaskType> listTaskTypes = _taskFactory.getAllTaskTypes( locale );
        Map<String, String> mapTaskTypes = new HashMap<>( );

        if ( CollectionUtils.isNotEmpty( listTaskTypes ) )
        {
            for ( ITaskType taskType : listTaskTypes )
            {
                mapTaskTypes.put( taskType.getKey( ), taskType.getTitle( ) );
            }
        }

        return mapTaskTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceWorkflow getInitialResourceWorkflow( int nIdResource, String strResourceType, Workflow workflow, Integer nExternalParentId )
    {
        ResourceWorkflow resourceWorkflow = null;
        StateFilter filter = new StateFilter( );
        filter.setIdWorkflow( workflow.getId( ) );
        filter.setIsInitialState( StateFilter.FILTER_TRUE );

        List<State> listInitialState = _stateService.getListStateByFilter( filter );

        if ( CollectionUtils.isNotEmpty( listInitialState ) )
        {
            resourceWorkflow = new ResourceWorkflow( );
            resourceWorkflow.setIdResource( nIdResource );
            resourceWorkflow.setResourceType( strResourceType );
            resourceWorkflow.setState( listInitialState.get( 0 ) );
            resourceWorkflow.setWorkFlow( workflow );
            resourceWorkflow.setExternalParentId( nExternalParentId );
        }

        return resourceWorkflow;
    }

    // CHECK

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canProcessAction( int nIdResource, String strResourceType, int nIdAction, Integer nIdExternalParent )
    {
        Action action = _actionService.findByPrimaryKey( nIdAction );

        if ( action != null )
        {
            ResourceWorkflow resourceWorkflow = _resourceWorkflowService.findByPrimaryKey( nIdResource, strResourceType, action.getWorkflow( ).getId( ) );

            if ( ( resourceWorkflow != null ) && ( resourceWorkflow.getState( ).getId( ) == action.getStateBefore( ).getId( ) ) )
            {
                if ( action.isAutomaticState( ) )
                {
                    return canAutomaticActionBeProcessed( nIdResource, strResourceType, nIdAction );
                }

                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisplayTasksForm( int nIdAction, Locale locale )
    {
        List<ITask> listTasks = _taskService.getListTaskByIdAction( nIdAction, locale );

        for ( ITask task : listTasks )
        {
            if ( task.getTaskType( ).isFormTaskRequired( ) )
            {
                return true;
            }
        }

        return false;
    }

    // DO

    /**
     * {@inheritDoc}
     */
    @Override
    public void doProcessAction( int nIdResource, String strResourceType, int nIdAction, Integer nIdExternalParent, HttpServletRequest request, Locale locale,
            boolean bIsAutomatic, String strUserAccessCode, User user )
    {
        Action action = _actionService.findByPrimaryKey( nIdAction );

        if ( action == null || !canProcessAction( nIdResource, strResourceType, nIdAction, nIdExternalParent ) )
        {
            return;
        }

        ResourceWorkflow resourceWorkflow = _resourceWorkflowService.findByPrimaryKey( nIdResource, strResourceType, action.getWorkflow( ).getId( ) );

        if ( resourceWorkflow == null )
        {
            resourceWorkflow = getInitialResourceWorkflow( nIdResource, strResourceType, action.getWorkflow( ), nIdExternalParent );

            if ( resourceWorkflow != null )
            {
                _resourceWorkflowService.create( resourceWorkflow );
            }
        }

        // Create ResourceHistory
        ResourceHistory resourceHistory = _resourceHistoryFactory.newResourceHistory( nIdResource, strResourceType, action, strUserAccessCode, bIsAutomatic,
                user );
        _resourceHistoryService.create( resourceHistory );

        List<ITask> listActionTasks = _taskService.getListTaskByIdAction( nIdAction, locale );

        for ( ITask task : listActionTasks )
        {
            task.setAction( action );

            try
            {
                task.processTask( resourceHistory.getId( ), request, locale, user );
            }
            catch( Exception e )
            {
                // Revert the creation of the resource history
                _resourceHistoryService.remove( resourceHistory.getId( ) );

                throw new WorkflowRuntimeException( "WorkflowService - Error when executing task ID " + task.getId( ), e );
            }
        }

        // Reload the resource workflow in case a task had modified it
        resourceWorkflow = _resourceWorkflowService.findByPrimaryKey( nIdResource, strResourceType, action.getWorkflow( ).getId( ) );
        resourceWorkflow.setState( action.getStateAfter( ) );
        resourceWorkflow.setExternalParentId( nIdExternalParent );
        _resourceWorkflowService.update( resourceWorkflow );

        if ( ( action.getStateAfter( ) != null ) && !action.isAutomaticReflexiveAction( ) )
        {
            if ( action.getStateBefore( ).getId( ) != action.getStateAfter( ).getId( ) )
            {
                doProcessAutomaticReflexiveActions( nIdResource, strResourceType, action.getStateAfter( ).getId( ), nIdExternalParent, locale );
            }

            State state = action.getStateAfter( );
            ActionFilter actionFilter = new ActionFilter( );
            actionFilter.setIdWorkflow( action.getWorkflow( ).getId( ) );
            actionFilter.setIdStateBefore( state.getId( ) );
            actionFilter.setIsAutomaticState( 1 );
            actionFilter.setAutomaticReflexiveAction( false );

            List<Action> listAction = _actionService.getListActionByFilter( actionFilter );

            if ( CollectionUtils.isNotEmpty( listAction ) && ( listAction.get( 0 ) != null ) )
            {
                doProcessAction( nIdResource, strResourceType, listAction.get( 0 ).getId( ), nIdExternalParent, request, locale, true, null, user );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doProcessAction( int nIdResource, String strResourceType, int nIdAction, Integer nIdExternalParent, HttpServletRequest request, Locale locale,
            boolean bIsAutomatic, String strUserAccessCode )
    {
        doProcessAction( nIdResource, strResourceType, nIdAction, nIdExternalParent, request, locale, bIsAutomatic, strUserAccessCode, null );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doProcessAutomaticReflexiveActions( int nIdResource, String strResourceType, int nIdState, Integer nIdExternalParent, Locale locale, User user )
    {
        State state = _stateService.findByPrimaryKey( nIdState );
        ActionFilter actionFilter = new ActionFilter( );
        actionFilter.setIdWorkflow( state.getWorkflow( ).getId( ) );
        actionFilter.setIdStateBefore( state.getId( ) );
        actionFilter.setAutomaticReflexiveAction( true );

        List<Action> listAction = _actionService.getListActionByFilter( actionFilter );

        if ( CollectionUtils.isNotEmpty( listAction ) )
        {
            for ( Action action : listAction )
            {
                doProcessAction( nIdResource, strResourceType, action.getId( ), nIdExternalParent, null, locale, true, null, user );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doProcessAutomaticReflexiveActions( int nIdResource, String strResourceType, int nIdState, Integer nIdExternalParent, Locale locale )
    {
        doProcessAutomaticReflexiveActions( nIdResource, strResourceType, nIdState, nIdExternalParent, locale, null );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRemoveWorkFlowResource( int nIdResource, String strResourceType )
    {
        for ( Workflow workflow : getListWorkflowsByFilter( new WorkflowFilter( ) ) )
        {
            doRemoveWorkFlowResource( nIdResource, strResourceType, workflow.getId( ) );
        }
    }

    /**
     * Remove in the workflow the resource specified in parameter
     * 
     * @param nIdResource
     *            the resource id
     * @param strResourceType
     *            the resource type
     * @param nIdWorkflow
     *            the workflow id
     */
    public void doRemoveWorkFlowResource( int nIdResource, String strResourceType, int nIdWorkflow )
    {
        List<ResourceHistory> listResourceHistoryToRemove;
        List<ITask> listTask = new ArrayList<>( );
        List<Action> listWorkflowAction;

        listResourceHistoryToRemove = _resourceHistoryService.getAllHistoryByResource( nIdResource, strResourceType, nIdWorkflow );

        ActionFilter actionFilter = new ActionFilter( );
        actionFilter.setIdWorkflow( nIdWorkflow );
        actionFilter.setAutomaticReflexiveAction( false );
        listWorkflowAction = _actionService.getListActionByFilter( actionFilter );

        for ( Action action : listWorkflowAction )
        {
            listTask.addAll( _taskService.getListTaskByIdAction( action.getId( ), Locale.getDefault( ) ) );
        }

        // Remove TaskInformation
        for ( ResourceHistory resourceHistory : listResourceHistoryToRemove )
        {
            for ( ITask task : listTask )
            {
                task.doRemoveTaskInformation( resourceHistory.getId( ) );
            }

            _resourceHistoryService.remove( resourceHistory.getId( ) );
        }

        // Remove resourceWorkflow
        ResourceWorkflow resourceWorkflowToRemove = _resourceWorkflowService.findByPrimaryKey( nIdResource, strResourceType, nIdWorkflow );

        if ( resourceWorkflowToRemove != null )
        {
            _resourceWorkflowService.remove( resourceWorkflowToRemove );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRemoveWorkFlowResourceByListId( List<Integer> lListIdResource, String strResourceType, Integer nIdWorflow )
    {
        List<Integer> listIdHistory = _resourceHistoryService.getListHistoryIdByListIdResourceId( lListIdResource, strResourceType, nIdWorflow );

        ActionFilter actionFilter = new ActionFilter( );
        actionFilter.setIdWorkflow( nIdWorflow );
        actionFilter.setAutomaticReflexiveAction( false );

        List<Action> listWorkflowAction = _actionService.getListActionByFilter( actionFilter );
        List<ITask> listTask = new ArrayList<>( );

        for ( Action action : listWorkflowAction )
        {
            listTask.addAll( _taskService.getListTaskByIdAction( action.getId( ), Locale.getDefault( ) ) );
        }

        for ( Integer nIdHistory : listIdHistory )
        {
            for ( ITask task : listTask )
            {
                task.doRemoveTaskInformation( nIdHistory );
            }
        }

        _resourceHistoryService.removeByListIdResource( lListIdResource, strResourceType, nIdWorflow );
        _resourceWorkflowService.removeByListIdResource( lListIdResource, strResourceType, nIdWorflow );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeActionAutomatic( int nIdResource, String strResourceType, int nIdWorkflow, Integer nExternalParentId, User user )
    {
        ResourceWorkflow resourceWorkflow = _resourceWorkflowService.findByPrimaryKey( nIdResource, strResourceType, nIdWorkflow );

        if ( ( resourceWorkflow != null ) && ( resourceWorkflow.getState( ) != null ) )
        {
            State state = resourceWorkflow.getState( );
            state = _stateService.findByPrimaryKey( state.getId( ) );

            ActionFilter actionFilter = new ActionFilter( );
            actionFilter.setIdWorkflow( state.getWorkflow( ).getId( ) );
            actionFilter.setIdStateBefore( state.getId( ) );
            actionFilter.setIsAutomaticState( 1 );
            actionFilter.setAutomaticReflexiveAction( false );

            List<Action> listAction = _actionService.getListActionByFilter( actionFilter );

            if ( ( listAction != null ) && !listAction.isEmpty( ) && ( listAction.get( 0 ) != null ) )
            {
                doProcessAction( nIdResource, strResourceType, listAction.get( 0 ).getId( ), nExternalParentId, null, null, true, null, user );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeActionAutomatic( int nIdResource, String strResourceType, int nIdWorkflow, Integer nExternalParentId )
    {
        executeActionAutomatic( nIdResource, strResourceType, nIdWorkflow, nExternalParentId, null );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getResourceIdListByIdState( int nIdState, String strResourceType )
    {
        List<ResourceWorkflow> listResourceWorkflow = _resourceWorkflowService.getAllResourceWorkflowByState( nIdState );

        if ( CollectionUtils.isEmpty( listResourceWorkflow ) )
        {
            return new ArrayList<>( );
        }

        List<Integer> listResourceId = new ArrayList<>( listResourceWorkflow.size( ) );

        for ( ResourceWorkflow resourceWorkflow : listResourceWorkflow )
        {
            if ( StringUtils.equals( strResourceType, resourceWorkflow.getResourceType( ) ) )
            {
                listResourceId.add( resourceWorkflow.getIdResource( ) );
            }
        }

        return listResourceId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAutomaticActionBeProcessed( int nIdResource, String strResourceType, int nIdAction )
    {
        for ( Prerequisite prerequisite : _prerequisiteManagementService.getListPrerequisite( nIdAction ) )
        {
            IAutomaticActionPrerequisiteService prerequisiteService = _prerequisiteManagementService
                    .getPrerequisiteService( prerequisite.getPrerequisiteType( ) );

            if ( !prerequisiteService.canActionBePerformed( nIdResource, strResourceType,
                    _prerequisiteManagementService.getPrerequisiteConfiguration( prerequisite.getIdPrerequisite( ), prerequisiteService ), nIdAction ) )
            {
                return false;
            }
        }

        return true;
    }
}
