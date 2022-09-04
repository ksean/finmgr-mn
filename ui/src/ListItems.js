/*
    finmgr - a financial management framework
    Copyright (C) 2022  Kennedy Software Solutions Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

    sean <at> kennedy <dot> software
 */
import * as React from 'react';
import { Link } from "react-router-dom";
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import {
    DashboardOutlined,
    Download,
    FilterAlt,
    FormatListBulleted,
    Photo,
    Publish,
    Receipt,
} from "@mui/icons-material";

export const mainListItems = (
    <React.Fragment>
        <Link to="/">
            <ListItemButton>
                <ListItemIcon>
                    <DashboardOutlined />
                </ListItemIcon>
                <ListItemText primary="Dashboard" />
            </ListItemButton>
        </Link>
        <ListItemButton>
            <ListItemIcon>
                <Receipt />
            </ListItemIcon>
            <ListItemText primary="Transactions" />
        </ListItemButton>
        <ListItemButton>
            <ListItemIcon>
                <Photo />
            </ListItemIcon>
            <ListItemText primary="Visualizations" />
        </ListItemButton>
        <ListItemButton>
            <ListItemIcon>
                <FormatListBulleted />
            </ListItemIcon>
            <ListItemText primary="Visual Types" />
        </ListItemButton>
        <ListItemButton>
            <ListItemIcon>
                <FilterAlt />
            </ListItemIcon>
            <ListItemText primary="Data Filters" />
        </ListItemButton>
    </React.Fragment>
);

export const secondaryListItems = (
    <React.Fragment>
        <Link to="/import">
            <ListItemButton>
                <ListItemIcon>
                    <Download />
                </ListItemIcon>
                <ListItemText primary="Import" />
            </ListItemButton>
        </Link>
        <ListItemButton>
            <ListItemIcon>
                <Publish />
            </ListItemIcon>
            <ListItemText primary="Export" />
        </ListItemButton>
    </React.Fragment>
);