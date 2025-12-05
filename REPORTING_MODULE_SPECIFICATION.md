# 📊 Uqar Pharmacy Management System - Reporting Module Specification

## 📋 **Document Overview**

This document serves as the **complete reference** for implementing the reporting feature in the Uqar Pharmacy Management System. It includes:

1. **SRS Requirements Analysis** - What the SRS specifies for reports
2. **Enhanced Features** - Additional features beyond SRS requirements
3. **Complete Module Structure** - Technical implementation design
4. **Implementation Roadmap** - Development phases and priorities

---

## 📖 **SRS Requirements Analysis**

### **3.5 التقارير والإحصائيات (Reports and Statistics)**

#### **3.5.1 تقارير المبيعات (Sales Reports)**

**SRS Requirements:**
- ✅ تقارير المبيعات اليومية (Daily Sales Reports)
  - إجمالي المبيعات (Total Sales)
  - عدد الفواتير (Invoice Count)
  - المنتجات الأكثر مبيعاً (Best-selling Products)
- ✅ تقارير المبيعات الشهرية (Monthly Sales Reports)
  - إجمالي المبيعات (Total Sales)
  - مقارنة مع الأشهر السابقة (Comparison with Previous Months)
  - رسوم بيانية (Charts)

#### **3.5.2 تقارير الأرباح (Profit Reports)**

**SRS Requirements:**
- ✅ تقارير الأرباح اليومية (Daily Profit Reports)
  - إجمالي الأرباح (Total Profits)
  - نسبة الربح (Profit Margin)
  - المنتجات الأكثر ربحية (Most Profitable Products)
- ✅ تقارير الأرباح الشهرية (Monthly Profit Reports)
  - إجمالي الأرباح (Total Profits)
  - مقارنة مع الأشهر السابقة (Comparison with Previous Months)
  - رسوم بيانية (Charts)

#### **3.5.3 تقارير المخزون (Inventory Reports)**

**SRS Requirements:**
- ✅ تقارير المخزون الحالي (Current Inventory Reports)
  - الكميات المتوفرة (Available Quantities)
  - المنتجات منخفضة المخزون (Low Stock Products)
  - المنتجات قريبة من انتهاء الصلاحية (Expiring Products)
- ✅ تقارير حركة المخزون (Inventory Movement Reports)
  - المنتجات الأكثر دوراناً (Fast-moving Products)
  - المنتجات الراكدة (Slow-moving Products)

#### **3.5.4 تقارير الديون (Debt Reports)**

**SRS Requirements:**
- ✅ تقارير ديون العملاء (Customer Debt Reports)
  - إجمالي الديون (Total Debts)
  - العملاء الأكثر مديونية (Most Indebted Customers)
  - الديون المتأخرة (Overdue Debts)

#### **3.5.5 تقارير الشراء (Purchase Reports)**

**SRS Requirements:**
- ✅ تقارير الشراء اليومية (Daily Purchase Reports)
  - إجمالي قيمة عمليات الشراء (Total Purchase Value)
- ✅ تقارير الشراء الشهرية (Monthly Purchase Reports)
  - إجمالي قيمة عمليات الشراء (Total Purchase Value)
  - مقارنة مع الأشهر السابقة (Comparison with Previous Months)
  - رسوم بيانية (Charts)

---

## 🆕 **Enhanced Features Beyond SRS**

### **🏢 System Admin Reports (Not in SRS)**

#### **Multi-Pharmacy Management**
```http
GET /api/v1/admin/reports/system-overview
GET /api/v1/admin/reports/pharmacy-performance
GET /api/v1/admin/reports/pharmacies/summary
GET /api/v1/admin/reports/pharmacies/comparison
GET /api/v1/admin/reports/pharmacies/performance-ranking
GET /api/v1/admin/reports/pharmacies/license-status
```

#### **User Management & Security**
```http
GET /api/v1/admin/reports/users/activity-summary
GET /api/v1/admin/reports/users/role-distribution
GET /api/v1/admin/reports/users/login-statistics
GET /api/v1/admin/reports/users/performance-metrics
GET /api/v1/admin/reports/security/audit-trail
GET /api/v1/admin/reports/security/access-logs
GET /api/v1/admin/reports/security/compliance-status
```

#### **System-wide Financial Analysis**
```http
GET /api/v1/admin/reports/financial/total-revenue
GET /api/v1/admin/reports/financial/currency-analysis
GET /api/v1/admin/reports/financial/debt-summary
GET /api/v1/admin/reports/financial/exchange-rate-impact
```

### **💰 MoneyBox Management Reports (Critical for Syrian Pharmacies)**

#### **Cash Management & Reconciliation**
```http
GET /api/v1/pharmacy/reports/moneybox/daily-balance
GET /api/v1/pharmacy/reports/moneybox/cash-flow
GET /api/v1/pharmacy/reports/moneybox/transaction-history
GET /api/v1/pharmacy/reports/moneybox/reconciliation-status
GET /api/v1/pharmacy/reports/moneybox/currency-breakdown
```

### **👥 Employee & Staff Management**

#### **Performance Tracking**
```http
GET /api/v1/pharmacy/reports/employees/performance-summary
GET /api/v1/pharmacy/reports/employees/sales-contribution
GET /api/v1/pharmacy/reports/employees/working-hours
GET /api/v1/pharmacy/reports/employees/shift-analysis
```

### **🔄 Advanced Analytics**

#### **Enhanced Sales Analysis**
```http
GET /api/v1/pharmacy/reports/sales/payment-methods
GET /api/v1/pharmacy/reports/sales/customer-analysis
GET /api/v1/pharmacy/reports/sales/refund-analysis
GET /api/v1/pharmacy/reports/sales/trend-analysis
```

#### **Advanced Inventory Analytics**
```http
GET /api/v1/pharmacy/reports/inventory/stock-valuation
GET /api/v1/pharmacy/reports/inventory/batch-tracking
GET /api/v1/pharmacy/reports/inventory/stock-turnover
GET /api/v1/pharmacy/reports/inventory/expiry-forecast
```

#### **Customer Relationship Management**
```http
GET /api/v1/pharmacy/reports/customers/payment-history
GET /api/v1/pharmacy/reports/customers/credit-analysis
GET /api/v1/pharmacy/reports/customers/purchase-patterns
GET /api/v1/pharmacy/reports/customers/loyalty-analysis
```

#### **Supplier & Purchase Analytics**
```http
GET /api/v1/pharmacy/reports/purchases/supplier-analysis
GET /api/v1/pharmacy/reports/purchases/cost-analysis
GET /api/v1/pharmacy/reports/purchases/order-status
GET /api/v1/pharmacy/reports/purchases/invoice-summary
```

---

## 🏗️ **Complete Module Structure**

### **📁 Directory Structure**

```
src/main/java/com/Uqar/reports/
├── controller/
│   ├── AdminReportController.java          # System Admin Reports
│   ├── PharmacyReportController.java       # Pharmacy Management Reports
│   └── ReportExportController.java        # Export functionality
├── service/
│   ├── AdminReportService.java
│   ├── PharmacyReportService.java
│   ├── ReportExportService.java
│   ├── ReportCalculationService.java
│   └── ReportCacheService.java
├── dto/
│   ├── request/
│   │   ├── ReportRequest.java
│   │   ├── DateRangeRequest.java
│   │   └── ExportRequest.java
│   ├── response/
│   │   ├── ReportResponse.java
│   │   ├── ChartData.java
│   │   ├── SummaryData.java
│   │   └── DetailData.java
│   └── common/
│       ├── ReportMetadata.java
│       └── FilterOptions.java
├── enums/
│   ├── ReportType.java
│   ├── ChartType.java
│   ├── ExportFormat.java
│   ├── TimePeriod.java
│   └── Currency.java
├── utils/
│   ├── ReportQueryBuilder.java
│   ├── ChartDataProcessor.java
│   ├── DateRangeCalculator.java
│   ├── CurrencyConverter.java
│   └── ReportFormatter.java
├── repository/
│   ├── ReportRepository.java
│   └── CustomReportRepository.java
└── config/
    ├── ReportConfig.java
    └── CacheConfig.java
```

### **📊 Report Categories & Endpoints**

#### **🏢 System Admin Reports** (`/api/v1/admin/reports`)

```http
# System Overview
GET /api/v1/admin/reports/system-overview
GET /api/v1/admin/reports/system-health
GET /api/v1/admin/reports/performance-metrics

# Multi-Pharmacy Management
GET /api/v1/admin/reports/pharmacies/summary
GET /api/v1/admin/reports/pharmacies/comparison
GET /api/v1/admin/reports/pharmacies/performance-ranking
GET /api/v1/admin/reports/pharmacies/license-status

# User Management
GET /api/v1/admin/reports/users/activity-summary
GET /api/v1/admin/reports/users/role-distribution
GET /api/v1/admin/reports/users/login-statistics
GET /api/v1/admin/reports/users/performance-metrics

# Financial Overview
GET /api/v1/admin/reports/financial/total-revenue
GET /api/v1/admin/reports/financial/currency-analysis
GET /api/v1/admin/reports/financial/debt-summary
GET /api/v1/admin/reports/financial/exchange-rate-impact

# Security & Compliance
GET /api/v1/admin/reports/security/audit-trail
GET /api/v1/admin/reports/security/access-logs
GET /api/v1/admin/reports/security/compliance-status
GET /api/v1/admin/reports/security/data-integrity

# Product Management
GET /api/v1/admin/reports/products/master-catalog
GET /api/v1/admin/reports/products/category-distribution
GET /api/v1/admin/reports/products/manufacturer-analysis
GET /api/v1/admin/reports/products/translation-coverage
```

#### **🏥 Pharmacy Management Reports** (`/api/v1/pharmacy/reports`)

```http
# Daily Operations
GET /api/v1/pharmacy/reports/daily/sales-summary
GET /api/v1/pharmacy/reports/daily/cash-position
GET /api/v1/pharmacy/reports/daily/inventory-status
GET /api/v1/pharmacy/reports/daily/customer-activity
GET /api/v1/pharmacy/reports/daily/employee-performance

# Sales & Revenue
GET /api/v1/pharmacy/reports/sales/period-summary
GET /api/v1/pharmacy/reports/sales/payment-methods
GET /api/v1/pharmacy/reports/sales/product-performance
GET /api/v1/pharmacy/reports/sales/customer-analysis
GET /api/v1/pharmacy/reports/sales/refund-analysis
GET /api/v1/pharmacy/reports/sales/profit-margin
GET /api/v1/pharmacy/reports/sales/trend-analysis

# Inventory Management
GET /api/v1/pharmacy/reports/inventory/current-stock
GET /api/v1/pharmacy/reports/inventory/expiry-alerts
GET /api/v1/pharmacy/reports/inventory/low-stock-alerts
GET /api/v1/pharmacy/reports/inventory/stock-movement
GET /api/v1/pharmacy/reports/inventory/stock-valuation
GET /api/v1/pharmacy/reports/inventory/batch-tracking
GET /api/v1/pharmacy/reports/inventory/stock-turnover
GET /api/v1/pharmacy/reports/inventory/expiry-forecast

# MoneyBox & Financial
GET /api/v1/pharmacy/reports/moneybox/daily-balance
GET /api/v1/pharmacy/reports/moneybox/cash-flow
GET /api/v1/pharmacy/reports/moneybox/transaction-history
GET /api/v1/pharmacy/reports/moneybox/reconciliation-status
GET /api/v1/pharmacy/reports/moneybox/currency-breakdown

# Customer Management
GET /api/v1/pharmacy/reports/customers/debt-summary
GET /api/v1/pharmacy/reports/customers/payment-history
GET /api/v1/pharmacy/reports/customers/credit-analysis
GET /api/v1/pharmacy/reports/customers/purchase-patterns
GET /api/v1/pharmacy/reports/customers/outstanding-balances
GET /api/v1/pharmacy/reports/customers/loyalty-analysis

# Purchase & Supplier
GET /api/v1/pharmacy/reports/purchases/order-summary
GET /api/v1/pharmacy/reports/purchases/supplier-analysis
GET /api/v1/pharmacy/reports/purchases/cost-analysis
GET /api/v1/pharmacy/reports/purchases/order-status
GET /api/v1/pharmacy/reports/purchases/invoice-summary

# Employee & Staff
GET /api/v1/pharmacy/reports/employees/performance-summary
GET /api/v1/pharmacy/reports/employees/sales-contribution
GET /api/v1/pharmacy/reports/employees/working-hours
GET /api/v1/pharmacy/reports/employees/shift-analysis

# Product Performance
GET /api/v1/pharmacy/reports/products/best-sellers
GET /api/v1/pharmacy/reports/products/slow-movers
GET /api/v1/pharmacy/reports/products/category-performance
GET /api/v1/pharmacy/reports/products/profit-contribution
GET /api/v1/pharmacy/reports/products/return-analysis
```

### **🔧 Technical Implementation Details**

#### **Request Structure**
```json
{
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "pharmacyId": "optional-for-admin",
  "currency": "SYP|USD|EUR",
  "groupBy": "day|week|month|year",
  "includeDetails": true,
  "includeCharts": true,
  "format": "json|pdf|excel|csv",
  "filters": {
    "productCategory": "Medicine",
    "paymentMethod": "Cash",
    "employeeId": "123"
  }
}
```

#### **Response Structure**
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalRecords": 150,
      "totalAmount": 50000.00,
      "currency": "SYP",
      "period": "2024-01-01 to 2024-01-31"
    },
    "details": [
      {
        "id": "1",
        "date": "2024-01-01",
        "amount": 1500.00,
        "description": "Daily sales"
      }
    ],
    "charts": {
      "pieChart": {
        "type": "pie",
        "data": [
          {"label": "Cash", "value": 70},
          {"label": "Credit", "value": 30}
        ]
      },
      "lineChart": {
        "type": "line",
        "data": [
          {"date": "2024-01-01", "value": 1500},
          {"date": "2024-01-02", "value": 1800}
        ]
      },
      "barChart": {
        "type": "bar",
        "data": [
          {"category": "Medicine", "value": 60},
          {"category": "Cosmetic", "value": 25},
          {"category": "Medical Supplies", "value": 15}
        ]
      }
    },
    "filters": {
      "appliedFilters": {
        "dateRange": "2024-01-01 to 2024-01-31",
        "currency": "SYP"
      },
      "availableFilters": [
        "productCategory",
        "paymentMethod",
        "employeeId"
      ]
    }
  },
  "metadata": {
    "generatedAt": "2024-01-31T23:59:59Z",
    "reportType": "daily-sales-summary",
    "pharmacyId": "pharmacy-123",
    "generatedBy": "user-456",
    "version": "1.0"
  }
}
```

### **🔐 Security & Access Control**

#### **Role-based Permissions**
```java
// System Admin - Full access to all reports
@PreAuthorize("hasRole('SYSTEM_ADMIN')")

// Pharmacy Manager - Access to pharmacy-specific reports
@PreAuthorize("hasRole('PHARMACY_MANAGER') and @pharmacyService.hasAccess(#pharmacyId)")

// Pharmacist - Limited access to operational reports
@PreAuthorize("hasRole('PHARMACIST') and @pharmacyService.hasAccess(#pharmacyId)")

// Trainee - Read-only access to basic reports
@PreAuthorize("hasRole('TRAINEE') and @pharmacyService.hasAccess(#pharmacyId)")
```

#### **Data Isolation**
- **Pharmacy-specific data** - Reports only show data for the user's pharmacy
- **Multi-pharmacy admin** - System admin can access all pharmacy data
- **Audit logging** - All report access is logged for compliance

### **📊 Chart Types & Visualizations**

#### **Supported Chart Types**
1. **Pie Charts** - Payment methods, product categories, currency distribution
2. **Line Charts** - Sales trends, cash flow, profit trends
3. **Bar Charts** - Product performance, employee comparison, monthly comparison
4. **Area Charts** - Cumulative sales, profit margins
5. **Tables** - Detailed data, summary tables
6. **Gauges** - Performance indicators, KPI metrics

### **📤 Export Functionality**

#### **Supported Formats**
1. **JSON** - API responses, data integration
2. **PDF** - Printable reports, official documentation
3. **Excel (.xlsx)** - Data analysis, spreadsheet integration
4. **CSV** - Simple data export, database import
5. **HTML** - Web-based reports, email integration

#### **Export Features**
- **Customizable templates** - Branded report layouts
- **Multi-language support** - Arabic and English reports
- **Batch export** - Multiple reports in one request
- **Scheduled exports** - Automated report generation
- **Email delivery** - Direct report delivery

---

## 🚀 **Implementation Roadmap**

### **Phase 1 - Core SRS Requirements (Week 1-2)**

#### **Priority 1: Daily Reports**
```http
GET /api/v1/pharmacy/reports/daily/sales-summary
GET /api/v1/pharmacy/reports/daily/cash-position
GET /api/v1/pharmacy/reports/inventory/current-stock
GET /api/v1/pharmacy/reports/customers/debt-summary
```

#### **Priority 2: Monthly Reports**
```http
GET /api/v1/pharmacy/reports/sales/period-summary
GET /api/v1/pharmacy/reports/sales/profit-margin
GET /api/v1/pharmacy/reports/purchases/order-summary
```

### **Phase 2 - Enhanced Features (Week 3-4)**

#### **MoneyBox Integration**
```http
GET /api/v1/pharmacy/reports/moneybox/daily-balance
GET /api/v1/pharmacy/reports/moneybox/cash-flow
GET /api/v1/pharmacy/reports/moneybox/transaction-history
```

#### **Advanced Analytics**
```http
GET /api/v1/pharmacy/reports/products/best-sellers
GET /api/v1/pharmacy/reports/employees/performance-summary
GET /api/v1/pharmacy/reports/sales/trend-analysis
```

### **Phase 3 - Admin Reports (Week 5-6)**

#### **System Admin Features**
```http
GET /api/v1/admin/reports/system-overview
GET /api/v1/admin/reports/pharmacy-performance
GET /api/v1/admin/reports/financial/currency-analysis
```

#### **Export & Advanced Features**
```http
POST /api/v1/reports/export
GET /api/v1/reports/scheduled
GET /api/v1/reports/templates
```

### **Phase 4 - Optimization & Enhancement (Week 7-8)**

#### **Performance Optimization**
- **Caching implementation** - Redis-based report caching
- **Query optimization** - Database query performance tuning
- **Pagination** - Large dataset handling
- **Real-time updates** - Live data refresh

#### **Advanced Features**
- **Custom report builder** - User-defined reports
- **Dashboard widgets** - Real-time KPI displays
- **Alert system** - Automated notifications
- **Mobile optimization** - Responsive design

---

## 📈 **Success Metrics**

### **Technical Metrics**
- **Response Time**: < 2 seconds for complex reports
- **Caching Hit Rate**: > 80% for frequently accessed reports
- **Export Performance**: < 5 seconds for PDF/Excel generation
- **Data Accuracy**: 100% consistency with source data

### **Business Metrics**
- **User Adoption**: 90% of target users using reports within 30 days
- **Report Usage**: Average 5+ reports per user per day
- **Export Usage**: 70% of users exporting reports regularly
- **Decision Support**: 80% of users making decisions based on reports

---

## 🎯 **Key Features Summary**

### **✅ SRS Requirements Coverage**
- **100% Coverage** of all SRS reporting requirements
- **Enhanced Implementation** with additional features
- **Multi-language Support** (Arabic/English)
- **Role-based Access** control

### **🆕 Enhanced Features**
- **System Admin Reports** for multi-pharmacy oversight
- **MoneyBox Integration** for cash management
- **Advanced Analytics** for business intelligence
- **Export Functionality** for business use
- **Real-time Charts** for data visualization

### **🏗️ Technical Excellence**
- **Scalable Architecture** for future growth
- **Performance Optimization** for large datasets
- **Security Implementation** for data protection
- **Caching Strategy** for improved performance
- **Audit Trail** for compliance

---

## 📋 **Next Steps**

1. **Review & Approval** - Stakeholder review of this specification
2. **Technical Design** - Detailed technical implementation design
3. **Database Optimization** - Query optimization and indexing
4. **Development Sprint** - Phase 1 implementation
5. **Testing & Validation** - Comprehensive testing
6. **Deployment** - Production deployment
7. **User Training** - End-user training and documentation

---

**Document Version**: 1.0  
**Created Date**: [Current Date]  
**Last Updated**: [Current Date]  
**Next Review**: [Date + 1 week]  
**Intended Use**: Development Reference & Implementation Guide
