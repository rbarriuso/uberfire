package org.uberfire.client.views.pfly.tab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwtbootstrap3.client.shared.event.TabShowEvent;
import org.gwtbootstrap3.client.shared.event.TabShowHandler;
import org.gwtbootstrap3.client.shared.event.TabShownEvent;
import org.gwtbootstrap3.client.shared.event.TabShownHandler;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabContent;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPanel;
import org.gwtbootstrap3.client.ui.constants.IconPosition;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A Bootstrap3 TabPanel which supports a mix of normal tabs and tabs that are dropdown menus. Selecting an item from a
 * dropdown menu tab causes that item's associated content to display in the tab panel's content area.
 */
public class TabPanelWithDropdowns extends Composite {

    /**
     * The bar at the top where the tabs sit.
     */
    protected final NavTabs tabBar;

    /**
     * The content area that shows the content for the currently selected tab.
     */
    protected final TabContent tabContent;

    /**
     * Widgets we have created that can have the CSS style name "active" added to them. When a new tab is selected, all
     * of these widgets get the "active" style removed from them.
     */
    private final Set<Widget> activatableWidgets = new HashSet<Widget>();

    /**
     * Removes the "active" style class from all widgets in {@link #activatableWidgets}.
     */
    private final TabShowHandler clearActiveStyles = new TabShowHandler() {

        @Override
        public void onShow( TabShowEvent showEvent ) {
            for ( Widget w : activatableWidgets ) {
                w.removeStyleName( Styles.ACTIVE );
            }
        }
    };

    private final Map<TabPanelEntry, HandlerRegistration> tabHandlerRegistrations = new IdentityHashMap<TabPanelEntry, HandlerRegistration>();

    /**
     * Creates an empty tab panel.
     */
    public TabPanelWithDropdowns() {
        tabBar = new NavTabs();
        tabContent = new TabContent();

        TabPanel root = new TabPanel();
        root.add( tabBar );
        root.add( tabContent );
        initWidget( root );
    }

    /**
     * Adds a normal tab (not a dropdown) with the given label and contents.
     *
     * @param label
     *            the label for the tab itself.
     * @param content
     *            the contents that should appear in the content area when the tab is selected.
     * @return the newly created entry object that ties together the tab widget and its contents.
     */
    public TabPanelEntry addItem( String label,
                                  Widget content ) {
        TabPanelEntry tab = new TabPanelEntry( label, content );
        addItem( tab );
        return tab;
    }

    /**
     * Adds a normal tab (not a dropdown) with the given label and contents.
     *
     * @param tab the label and contents associated with the new tab.
     */
    public void addItem( TabPanelEntry tab ) {
        tabHandlerRegistrations.put( tab, tab.getTabWidget().addShowHandler( clearActiveStyles ) );
        activatableWidgets.add( tab.getTabWidget() );
        tabBar.add( tab.getTabWidget() );
        tabContent.add( tab.getContentPane() );
    }
    /**
     * Removes the given tab and its associated contents that were previously added with
     * {@link #addItem(String, Widget)}. Has no effect if the item is not currently in this tab panel.
     *
     * @param tab
     *            the item to remove.
     */
    public boolean remove( TabPanelEntry tab ) {
        if ( tabHandlerRegistrations.get( tab ) != null ) {
            tabHandlerRegistrations.remove( tab ).removeHandler();
        }
        boolean removed = tabBar.remove( tab.getTabWidget() );
        activatableWidgets.remove( tab.getTabWidget() );
        tab.getContents().asWidget().removeFromParent();
        return removed;
    }

    /**
     * Adds a new tab to this panel that doesn't have any contents itself, but can contain multiple items that appear in
     * a dropdown menu when the tab is clicked. This dropdown menu is initially empty. Items can be added and removed
     * using the {@link DropDownTab#addItem(String, Widget)} and
     * {@link DropDownTab#removeItem(String, Widget)} methods.
     *
     * @param label
     *            The text that should appear on the dropdown tab.
     * @return the container for the items that appear when the tab is clicked.
     */
    public DropDownTab addDropdownTab( String label ) {

        AnchorListItem tab = new AnchorListItem( label );

        // FIXME should actually subclass AnchorListItem and add a <b class=caret/> to the anchor elem
        tab.setIcon( IconType.CARET_DOWN );
        tab.setIconPosition( IconPosition.RIGHT );

        tab.addStyleName( Styles.DROPDOWN_TOGGLE );
        tab.setDataToggle( Toggle.DROPDOWN );

        DropDownTab dropDownTab = new DropDownTab( tab );
        tab.add( dropDownTab );
        addDropdownTab( dropDownTab );

        return dropDownTab;
    }

    /**
     * Adds a pre-made dropdown tab to this tab panel. This can be used for re-attaching a dropdown tab that was
     * previously added with {@link #addDropdownTab(String)} and then removed.
     *
     * @param tab the tab to add back
     */
    public void addDropdownTab( DropDownTab contents ) {
        AnchorListItem tab = contents.owningTab;

        // gets set to active when one of the menu items is selected
        activatableWidgets.add( tab );
        tabBar.add( tab );
    }

    /**
     * Container for the menu items that appear when the dropdown tab is clicked. Normally, should only be created by
     * {@link TabPanelWithDropdowns#addDropdownTab(String)}.
     */
    public class DropDownTab extends DropDownMenu {

        private final AnchorListItem owningTab;
        private final List<TabPanelEntry> contents = new ArrayList<TabPanelEntry>();
        private final Multimap<TabPanelEntry, HandlerRegistration> tabHandlerRegistrations = HashMultimap.create();

        public DropDownTab( AnchorListItem owningTab ) {
            this.owningTab = owningTab;
            addStyleName( "uf-dropdown-tab-menu-container" );
        }

        public TabPanelEntry addItem( String label,
                             Widget content ) {
            TabPanelEntry tab = new TabPanelEntry( label, content );
            addItem( tab );
            return tab;
        }

        public void addItem( TabPanelEntry tab ) {
            tab.setInDropdown( true );
            contents.add( tab );

            TabListItem tabWidget = tab.getTabWidget();
            activatableWidgets.add( tabWidget );
            tabHandlerRegistrations.put( tab, tabWidget.addShowHandler( clearActiveStyles ) );
            tabHandlerRegistrations.put( tab, tabWidget.addShownHandler( new TabShownHandler() {

                @Override
                public void onShown( TabShownEvent event ) {
                    DropDownTab.this.getParent().addStyleName( Styles.ACTIVE );
                }
            } ) );

            add( tabWidget );
            tabContent.add( tab.getContentPane() );
        }

        /**
         * Sets the text that appears on this dropdown's main tab.
         *
         * @param text the new label for the dropdown tab.
         */
        public void setText( String text ) {
            owningTab.setText( text );
        }

        @Override
        public void clear() {
            for ( TabPanelEntry tab : contents ) {
                tab.getContentPane().removeFromParent();
                tab.getTabWidget().removeFromParent();

                for ( HandlerRegistration handlerRegistration : tabHandlerRegistrations.removeAll( tab ) ) {
                    handlerRegistration.removeHandler();
                }

                activatableWidgets.remove( tab.getTabWidget() );
            }
            contents.clear();
        }
    }

    /**
     * Removes all tabs and content from this tab panel.
     */
    public void clear() {
        tabBar.clear();
        tabContent.clear();
        activatableWidgets.clear();
        // TODO clear all handler registrations
    }
}