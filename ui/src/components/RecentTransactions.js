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
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Title from '../Title';
import { Tooltip } from "@mui/material";

class RecentTransactions extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            error: null,
            isLoading: true,
            rows: [],
        }
    }

    componentDidMount() {
        fetch('/investment/latest', {
            mode: 'no-cors'
        })
        .then(response => {
            console.log(response)
            if (response.status === 200) {
                response.json().then (data => {
                    this.setState({
                        isLoading: false,
                        rows: data
                    });
                })
            } else if (response.status === 500) {
                this.setState({
                    isLoading: false,
                    error: { 'message': 'API server isn\'t started' }
                });
            } else {
                this.setState({
                    isLoading: false,
                    error: response
                });
            }
        })
        .catch(error => {
            console.log(error)
            this.setState({
                isLoading: false,
                error
            });
        })
    }

    render() {
        const {error, isLoading, rows} = this.state;

        const formatDate = (string) => {
            const date = new Date(string * 1000)
            return date.toISOString().substring(0, 10)
        }

        const formatCurrency = (number) => {
            if (number == null) {
                return ''
            } else {
                return number.toFixed(2)
            }
        }

        if (error) {
            return <div>Error: {error.message}</div>
        } else if (isLoading) {
            return <div>Loading...</div>
        } else {
            return (
                <React.Fragment>
                    <Title sx={{ pl: 8 }}>Recent Transactions</Title>
                    <Table size='large'>
                        <TableHead>
                            <TableRow>
                                <TableCell>Transaction Date</TableCell>
                                <TableCell>Settlement Date</TableCell>
                                <TableCell>Action</TableCell>
                                <TableCell>Symbol</TableCell>
                                <TableCell>Description</TableCell>
                                <TableCell>Quantity</TableCell>
                                <TableCell>Price ($)</TableCell>
                                <TableCell>Commission ($)</TableCell>
                                <TableCell>Net Amount ($)</TableCell>
                                <TableCell>Currency</TableCell>
                                <TableCell>Account</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {rows.map((row) => {
                                return <TableRow key={row.id}>
                                    <TableCell>{formatDate(row.transactionDate)}</TableCell>
                                    <TableCell>{formatDate(row.settlementDate)}</TableCell>
                                    <TableCell>{row.action}</TableCell>
                                    <TableCell>{row.symbol.value}</TableCell>
                                    <TableCell width={200}>
                                        <Tooltip title={row.description}><div style={{
                                            whiteSpace: "nowrap",
                                            textOverflow: "ellipsis",
                                            width: "200px",
                                            display: "block",
                                            overflow: "hidden"
                                        }}>{row.description}</div></Tooltip>
                                    </TableCell>
                                    <TableCell>{row.quantity}</TableCell>
                                    <TableCell>{formatCurrency(row.price)}</TableCell>
                                    <TableCell>{formatCurrency(row.commission)}</TableCell>
                                    <TableCell>{formatCurrency(row.net)}</TableCell>
                                    <TableCell>{row.currency}</TableCell>
                                    <TableCell>{row.account.alias}</TableCell>
                                </TableRow>
                            })}
                        </TableBody>
                    </Table>
                </React.Fragment>
            )
        }
    }
}

export default RecentTransactions;