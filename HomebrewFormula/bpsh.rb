class Bpsh < Formula
  desc "DS3 Script shell for interfacing with the Spectra Logic BlackPearl"
  homepage "https://github.com/SpectraLogic/ds3_script"
  url "https://github.com/SpectraLogic/ds3_script/releases/download/v3.5.0/bpsh-3.5.0.tar"
  sha256 "551d56e1f2d3359a99c736a7721a8e26b4781a8b117b61cb5f487b43781bfeca"

  bottle :unneeded

  depends_on :java => "1.8+"

  def install
    rm_f Dir["bin/*.bat"]
    libexec.install %w[bin lib]
    (bin/"bpsh").write_env_script libexec/"bin/bpsh", Language::Java.overridable_java_home_env
  end

  test do
    assert_match version.to_s, shell_output("#{bin}/bpsh -v")
  end
end
