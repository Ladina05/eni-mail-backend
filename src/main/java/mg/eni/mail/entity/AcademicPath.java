package mg.eni.mail.entity;

public enum AcademicPath {
    SOFTWARE_ENGINEERING_DATABASES(AcademicMention.COMPUTER_SCIENCE),
    SYSTEMS_NETWORKS_ADMINISTRATION(AcademicMention.COMPUTER_SCIENCE),
    GENERAL_COMPUTING(AcademicMention.COMPUTER_SCIENCE),
    DATA_GOVERNANCE_ENGINEERING(AcademicMention.ARTIFICIAL_INTELLIGENCE),
    CONNECTED_OBJECTS_CYBERSECURITY(AcademicMention.ARTIFICIAL_INTELLIGENCE),
    DIGITAL_JOBS(AcademicMention.DIGITAL_EXPERTISE),
    INFORMATION_SYSTEMS_AUDIT(AcademicMention.DIGITAL_EXPERTISE);

    private final AcademicMention mention;
    AcademicPath(AcademicMention mention) { this.mention = mention; }
    public AcademicMention getMention() { return mention; }
}
