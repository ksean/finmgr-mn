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
package sh.kss.finmgr.service.impl;

import jakarta.inject.Singleton;
import sh.kss.finmgr.domain.InvestmentTransaction;
import sh.kss.finmgr.persistence.InvestmentTransactionRepository;
import sh.kss.finmgr.service.InvestmentTransactionService;

import java.util.Collection;
import java.util.List;

@Singleton
public class InvestmentTransactionServiceImpl implements InvestmentTransactionService {

    private final InvestmentTransactionRepository repository;

    public InvestmentTransactionServiceImpl(InvestmentTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<InvestmentTransaction> findAll() {
        return repository.listAllOrderByTransactionDateAsc();
    }

    @Override
    public List<InvestmentTransaction> getLatest() {
        return repository.listTop5OrderByTransactionDateDesc();
    }

    @Override
    public void saveAll(Collection<InvestmentTransaction> transactions) {
        repository.saveAll(transactions);
    }
}