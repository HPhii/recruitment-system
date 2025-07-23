package com.example.demo.Service;

import com.example.demo.entity.Cv;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class CvAiExtractorService {

    @Value("${nvidia.api.key:}")
    private String apiKey;

    private static final String API_ENDPOINT = "https://integrate.api.nvidia.com/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            Extract the following fields from the CV and return ONLY the raw JSON object with this structure:
            Match skills from the CV with the following list and return only the matched skills:
            Budget Control, Finance, Finance And Cost Control Skills, Financial Control, Real Estate, Bất Động Sản, Kiểm Soát Dự Án, Quản Lý Dự Án, Quản Lý Dự Án Cảnh Quan, Xây Dựng, Triển Khai Phần Cứng, Robotic, Tự Động Hóa, Giáo Dục Đào Tạo, STEM Education, Tài Chính, Báo Cáo Tài Chính, Kế Toán Chi Phí, Theo Dõi Công Nợ, Hạch Toán, Supply Chain Management, Import & Export, Logistics, Shipment Management, Shipping Management, ERP, Mô Hình Tài Chính, Phân Tích Tài Chính, Tài Chính Doanh Nghiệp, VAS, Material Planning, MRP, ERP Systems, Production Material, Production Monitoring, Product Development, Quality Control, Technical Engineering, Technical Support, Communication, Xi Mạ Vàng, Surface Design, Chemical, Chemistry, Inspection, Brand Strategy Development, Brand Strategy Development, Integrated Marketing Campaign, Integrated Planning, Marketing Campaign Management, Generative AI, Marketing Lead, Công Nghệ Xanh, Giải Quyết Tố Cáo, Kỹ năng giao tiếp, Kỹ năng giao tiếp, Lãnh Đạo, Mô Phỏng Phân Tích, Thiết Kế Nội Thất, AutoCAD, Bóc tách bản vẽ kỹ thuật, Diễn Họa 3D, SketchUp Render, Bảo Hành Máy Móc, Kỹ Thuật Điện, Sửa Chữa Thiết Bị, Hệ Thống Điện, Kinh Doanh, Bán Hàng, Quản Trị Kinh Doanh, Bán Hàng Dược Phẩm, Communications, HRBP, Talent Acquisition, English proficiency, Stakeholder Management, Giảng Dạy, Quản Lý Lớp Học, Soạn Thảo Giáo Án, Sư Phạm, Bảo Hiểm, Product Pricing, Ifrs17 Reporting, Pricing Management, Làm Việc Nhóm, Lập Kế Hoạch, Quản Lý, Quan Sát, Xử Lý Sự Cố, Kinh Doanh Thị Trường, Nghiên Cứu Thị Trường, Phân Tích Thông Tin, Phát triển thị trường, Quản Lý Chương Trình Khuyến Mãi, Creative Mindset, Data Analysis, Digital Storytelling, Marketing Management, Strategic Planning, Test Lab, Garment Testing, Color Management, Colour Matching, Color Checking, Giao Tiếp Tiếng Anh, Food & Beverage, Quản Lý Nhà Hàng Khách Sạn, Điều Hành Dịch Vụ Du Lịch, Revit MEP, MEP Design, Shopdrawing, Trade Marketing, Retail Marketing, Dealer Marketing, Marketing Activation, Event Support, Quản Trị Nhân Lực, C&B (Chế độ & Lương thưởng), Total Reward, Xây Dựng Chính Sách, Kinh Tế Lao Động, C++, C/C++, Software Development, OOP, Accounting, Cash Flow Report, English, SAP, Outsourcing, Kế Toán, Quản Lý Tài Chính, Tiếng Trung, Xây Dựng Dân Dụng, Khảo Sát Công Trình, Thiết Kế Xây Dựng, Hạ Tầng Viễn Thông, Autocad, Risk Analysis, Risk Management, Quản Lý Rủi Ro, Phân Tích Rủi Ro, Ngân hàng, Adobe Premiere Pro, Photography, Video Editing, Video Production, Automation Engineering, Customer Service, Electrical Engineering, Laser Machine, Giải Quyết Vấn Đề, Kinh Tế, Quản Trị Nhân Sự, QA, QC, Quality Internal Audit, Electronic Testing, Chất Lượng Linh Kiện, Dịch Vụ Khách Hàng, Giao Tiếp, Kỹ năng lập kế hoạch, Triển Khai Đào Tạo, Lean Manufacturing, Mechanical Engineering, Process Improvement, Project Management, Giải Pháp Chuỗi Cung Ứng, Mua Hàng, Quản Lý Đơn Hàng, Xuất nhập khẩu, Quản Lý Sản Xuất, Tiếng Nhật, Thiết Kế Khuôn Mẫu, 2D Drawing, Kế Hoạch Sản Xuất, Civil 3D, Giám Sát Dự Án Công Trình, Quản Lý Nhân Sự, C&B, Chính Sách Nhân Sự, Vận hành Nhân Sự, 3D Studio Max, Revit, Sketchup, Autodesk, Marketing Strategy, influencer marketing, Social Media Management, Content Strategy, Analytical Thinking, Điều Dưỡng Đa Khoa, Y Dược, Tiếng Đức, Chăm Sóc Sức Khỏe, Y Tế, Commercial Leasing, Research, Retail industry knowledge, Planning, Bản Vẽ Kỹ Thuật, Bóc Tách Khối Lượng, Công Nghệ Xây Dựng, Thiết Kế 3D, Business English, Inspiring And Motivating Others, MS Office, Persuasion, Sales Solution, Korean Language, IT Communications, Interpretation, Phiên Dịch, Phiên Dịch Tài Liệu, Phân Tích Số Liệu, Báo Cáo Quản Trị, Quản Trị Chi Phí, Kế Toán Tổng Hợp, Kế Toán Giá Thành, Phân Tích Báo Cáo Tài Chính, Đối Chiếu Số Liệu, Kế Toán Thuế, Quyết Toán Thuế, Luật Thuế, Báo Cáo Thuế, Kiểm Toán, Chief Accountant Certificate, Excel, Kiểm soát hàng hóa, Phần mềm kế toán, Quản Lý Kho, Quản Lý Tồn Kho, Quality Assurance, Regulatory Affair, Đảm Bảo Chất Lượng, Dược Phẩm, Pharmacy, Quản Lý Văn Phòng, Lễ Tân Hành Chính, Công Tác Văn Thư, Mua Sắm Thiết Bị, Admintration, Automotive Technology, Maintenance, After-Sales Support, Quality Management, Automotive Engineering, Back End Developer, Fullstack Development, Machine Learning, Python, Quản Lý Giáo Dục, Education, Education Management, Giáo Dục Mầm Non, C&B (lương, thưởng, phúc lợi), Nhân sự C&B, Nhân sự tổng hợp, Lương và phúc lợi, Ai, Canva, Canva, Canva, Photoshop, Thiết Kế Đồ Họa, Bussiness Development, Production, Shipment Planning, Hydraulics, Industrial Maintenance, Mechanical, Piping Installation, Safety Procedures, Automation, Engine Systems, Mechatronics, SCADA System, PLC, E-commerce, English fluency, Business Administration, Pricing Experience, HR, Chinese, Freight, Healthcare Industry, Human Resources, Recruitment, Labor Laws Knowledge, Administration, Training, Graphic Design, Teaching Art, Arts Education, Design Drawing, Thiết Kế Tủ Điện, Tiếng Anh, Lập Hồ Sơ Dự Thầu, Lập Trình PLC, Sales Engineering, Máy Lạnh Chiller, Chiller System, HVAC System, Điều Hòa Không Khí, Nhân Sự, Quan Hệ Lao Động, Đào Tạo và Phát Triển, ArchiCAD, Relationship Management, Financial Services, Banking, Tư Vấn Tài Chính, E-commerce Management, Sales Development, E-commerce Management, E-commerce Management, Marketing, Sales, Bán Hàng Kỹ Thuật, Đàm Phán Hợp Đồng, Giao Tiếp Hiệu Quả, Kỹ năng làm việc nhóm, Kỹ Năng Chốt Sale, Adobe Illustrator, Design, Graphics, Graphics Software, Hardware, IT Helpdesk, Microsoff Office 365, Network Technology, software, software, Legal, Law, Legal Compliance, Verbal Communication, Customer Care, Problem-solving, Microsoft Excel, Attention To Detail, Kỹ Thuật Điện Tử, Kiểm Tra Bo Mạch Điện Tử, Sửa Chữa, Garment Inspection, Problem Solving, Creative Thinking, Communication Skills, Operational Excellence, Quản Lý Kỹ Thuật, Đọc Bản Vẽ Kỹ Thuật, Điện Lạnh, Vận Hành Máy Dán, Chất Lượng Sản Phẩm, Cơ Điện, Bảo Trì, Khắc Phục Sự Cố Máy, PCCC, Dự Toán, Phòng cháy chữa cháy (PCCC), Chăm Sóc Khách Hàng, Phát Triển Thị Trường, Phát Triển Đại Lý, Bán hàng, Tiếp thị, SCADA, OEE, Bảo Trì Cơ Khí, Vận Hành Máy Móc, Vận Hành Máy CNC, Hàn Tự Động, Máy Tiện, Tiếng Hàn, Data Administration, Hadoop, Quản Lý Dữ Liệu, Spark, NoSQL, Operation Solution Design, Software Solution Architecture, Solution Architecture, Solution Design, Technical Solution Design, Đàm Phán, Quản Lý Thời Gian, Thiết Kế Quy Hoạch, Giám Sát Thi Công, Giao Thông Vận Tải, Công trình thiết kế, Cầu Đường, An Ninh Mạng, An Toàn Thông Tin, API Service, Testing, Flutter, Android Studio, Kotlin, State Management, BIM Training, Critical Thinking, Process Design, Teamwork, Time Management, Lập Báo Cáo Tài Chính, MISA, Nghiệp Vụ Kế Toán, Phân Tích Dữ Liệu, Tìm Kiếm Khách Hàng, Tư Vấn Dịch Vụ, Hr Recruitment Passion, Interpersonal Communication, Multicultural Awareness, Results Driven, Team Work, Bán Thiết Bị Máy Móc, Phát Triển Khách Hàng, Tư Vấn Giải Pháp, Trợ Lý, Nghiệp Vụ Thư Ký, Tổ Chức Thời Gian, UX Design, UI Design, UX Research, Tài Chính Ngân Hàng, Quan Hệ Khách Hàng Cá Nhân, Tín Dụng Ngân Hàng, Tư Vấn Tín Dụng, Biomedical Engineering, IVD Rapid Test, Biomedical, Technical Service, Công Nghệ Thực Phẩm, Hóa Sinh Thực Phẩm, Kiểm Tra Chất Lượng, Giám Sát Quy Trình Sản Xuất, Phân Tích Thị Trường, Quản Lý Danh Mục, Kỹ năng đàm phán, Làm Việc Chịu Áp Lực Cao, Quản Lý Chất Lượng, Quản Lý Hệ Thống HSEQ, Thuyết Phục, Viết Báo Cáo, Tài chính Ngân hàng, Phê Duyệt Tín Dụng, Giản Ngân Tín Dụng, Kiểm Soát Hồ Sơ Tín Dụng, Tín Dụng Cá Nhân, Dược, ETC, Kinh Nghiệm Trình Dược, Tư Vấn, Phát Triển Kinh Doanh, Business Management, Sale Management, Phát Triển Đội Ngũ Kinh Doanh, Quản Lý Khách Hàng, Quản Lý Kinh Doanh, Sales Planning, HVAC, HVAC Control Systems, Support Engineer, HVAC Design, Quan Hệ Khách Hàng, Thuyết Phục Đàm Phán, Tư Vấn Giao Dịch, Huy Động Vốn, Thiết Bị Y Tế, Kế Hoạch Bán Hàng, Business Development, Finding Customers, Market Expansion, Market Expansion, Market Expansion, Market Expansion, Nhân viên kinh doanh, Sale thị trường, Mechanical Knowledge, Mechanical Processing, Quản lý chất lượng, Kiểm Định Chất Lượng, Phân tích dữ liệu, Sử Dụng Phần Mềm, CPR, First Aid Training, Health Check, Health Medical Care, Nursing, Analytical Skills, Financial Reporting, Fixed Asset Management, General Accounting, Tax Compliance, Lập Dự Toán, Quản Lý Chi Phí, Hồ Sơ Mời Thầu, Thiết Kế Biện Pháp Thi Công, React, Android, iOS Design, Mobile Development, Swift, Java/Kotlin, SQLite, Phát Triển Sản Phẩm, Bảo Mật Thông Tin, Quản lý mạng, Database Administration, MS SQL, Oracle, PostgreSQL, An Toàn Công Trường, Shop Drawing, Triển Khai Bản Vẽ, Tin Học Văn Phòng, Báo Giá, Làm Hồ Sơ Thanh Quyết Toán, Thẩm Định, Lập Đồ Án Quy Hoạch, Phát Triển Tour Du Lịch, Chiến Lược Kinh Doanh, Tiếp Thị Sản Phẩm, Market Analysis, Application Consultation, English Communication, CNC, Gia Công Cơ Khí, Kinh Doanh Kỹ Thuật, Control 5S, SPI, Repair Maintenance, Lập Kế Hoạch Kinh Doanh, Soạn Thảo Văn Bản, PLC Mitsubishi, PLC Mitsubishi, Scenario Planning, Điện tử, Điều Khiển Máy, Enterprise IT Infrastructure, ICT Solutions, Devsecops Knowledge, ITSM, Sales Cycle Management, Adobe Photoshop, 3D Max, Illustrator, Illustrator, Solid Edge, Cơ Khí Chế Tạo, Lắp Ráp, Cải Tiến Thiết Kế, Project Manager Qualification, PMP, Tư Vấn Hồ Sơ, IFRS, Tax Accounting, Firewall, Pentesting Tools, Phân Tích Mã Độc, SIEM, Presales Technical, Chuyển đổi số, Giải Pháp Công Nghệ, Giải Pháp Ảo Hoá, Polymer Technology, English Reading, Experiment Design, Nghiên Cứu Vật Liệu, Thí Nghiệm, Máy Nén Khí, Tiếng Anh thành thạo, Tiếng Trung Thành Thạo, Dự Toán Xây Dựng, Kinh Tế Xây Dựng, Bảo Mật Hệ Thống, Phát Hiện Mã Độc, CEH, An Toàn Lao Động, Môi Trường, Quản Lý Chất Thải, Quản Lý Hóa Chất, ISO, Tín Dụng Doanh Nghiệp, Khách Hàng Doanh Nghiệp
            {
              "fullName": "",
              "email": "",
              "phone": "",
              "education": "",
              "experience": "",
              "skills": [""]
            }
            Do NOT include any additional text, explanations, or reasoning, including <think> tags or comments. Return the JSON object alone.
            """;

    public Cv extractCvEntityFromText(String cvText) {
        if (cvText == null || cvText.trim().isEmpty()) {
            throw new IllegalArgumentException("CV text cannot be null or empty");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("NVIDIA API key is not configured");
            }
            headers.setBearerAuth(apiKey);

            String userPrompt = "CV Content: " + cvText.trim();
            log.info("Sending prompt to AI: {}", userPrompt);

            var body = Map.of(
                    "model", "nvidia/llama-3.1-nemotron-ultra-253b-v1",
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.6,
                    "max_tokens", 4096
            );

            HttpEntity<?> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_ENDPOINT, request, String.class);

            if (response.getStatusCode().isError()) {
                log.error("API returned error: {} - {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("API error: " + response.getStatusCode() + " - " + response.getBody());
            }

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("Received empty or null response from AI API");
            }

            if (responseBody.trim().startsWith("<")) {
                log.error("Received HTML response instead of JSON: {}", responseBody);
                throw new RuntimeException("API returned HTML instead of JSON");
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody)
                    .path("choices").get(0)
                    .path("message")
                    .path("content");

            if (jsonNode.isMissingNode() || jsonNode.isNull()) {
                throw new RuntimeException("Invalid or missing content in AI response");
            }

            String content = jsonNode.asText();
            log.info("AI Response Content: {}", content);

            JsonNode dataNode;
            try {
                dataNode = objectMapper.readTree(content);
            } catch (Exception e) {
                log.error("Failed to parse AI response as JSON: {}", content, e);
                throw new RuntimeException("Failed to parse AI response as JSON", e);
            }

            return Cv.builder()
                    .fullName(getTextOrEmpty(dataNode, "fullName"))
                    .email(getTextOrEmpty(dataNode, "email"))
                    .phone(getTextOrEmpty(dataNode, "phone"))
                    .education(getTextOrEmpty(dataNode, "education"))
                    .experience(getTextOrEmpty(dataNode, "experience"))
                    .extractedSkills(extractSkills(dataNode))
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("API error while extracting CV: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("Failed to connect to AI API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error extracting CV using AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract CV using AI", e);
        }
    }

    private String getTextOrEmpty(JsonNode node, String field) {
        return node.path(field).isMissingNode() ? "" : node.path(field).asText();
    }

    private List<String> extractSkills(JsonNode node) {
        return StreamSupport.stream(node.path("skills").spliterator(), false)
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .toList();
    }
}